# CLAUDE.md — Dugout

## Project Name

**Dugout** — the official name of this application, used in the UI, repository name,
documentation, and any future branding. The repository should be named `dugout`.
References to "attendance-manager" in paths and package names should use `dugout` instead
(e.g. `com.dugout`, folder `dugout/`, Docker image `dugout-app`).

## Project Overview

Dugout is a web application for managing player attendance at training sessions and matches
for sports clubs. It replaces a Google Sheets + Apps Script system with a proper web app
that coaches (referred to as "mister" in Italian sports culture) can use from any browser.

The app is built to run locally (on the coach's machine via Docker) with the option to
deploy to a remote server in the future without code changes.

**Official language**: English (code, UI, documentation). Italian is available as a
switchable language via Spring i18n.

---

## Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 17 (LTS) |
| Framework | Spring Boot | 3.2.x |
| ORM | Spring Data JPA + Hibernate | included in Spring Boot |
| Security | Spring Security | included in Spring Boot |
| Database | PostgreSQL | 15 |
| Template engine | Thymeleaf | included in Spring Boot |
| CSS framework | Bootstrap | 5.3.x (via CDN or WebJars) |
| Build tool | Maven | 3.9.x |
| Containerization | Docker + Docker Compose | latest stable |
| i18n | Spring MessageSource | included in Spring Boot |

**Do not introduce additional frameworks or dependencies** beyond what is listed above
unless strictly necessary and explicitly requested. Keep the stack minimal and familiar.

---

## Project Structure

```
attendance-manager/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── attendancemanager/
│       │           ├── AttendanceManagerApplication.java
│       │           ├── config/
│       │           │   ├── SecurityConfig.java
│       │           │   ├── WebMvcConfig.java          # i18n locale resolver
│       │           │   └── DataInitializer.java       # Admin seed on first run
│       │           ├── controller/
│       │           │   ├── AuthController.java        # /login, /logout
│       │           │   ├── DashboardController.java   # / (home after login)
│       │           │   ├── TeamController.java        # /teams/**
│       │           │   ├── PlayerController.java      # /players/**
│       │           │   ├── EventController.java       # /events/**
│       │           │   ├── AttendanceController.java  # /attendance/**
│       │           │   ├── AdminController.java       # /admin/**
│       │           │   └── LanguageController.java    # /language
│       │           ├── service/
│       │           │   ├── CoachService.java
│       │           │   ├── TeamService.java
│       │           │   ├── PlayerService.java
│       │           │   ├── EventService.java
│       │           │   ├── AttendanceService.java
│       │           │   └── StatisticsService.java
│       │           ├── repository/
│       │           │   ├── CoachRepository.java
│       │           │   ├── TeamRepository.java
│       │           │   ├── PlayerRepository.java
│       │           │   ├── EventRepository.java
│       │           │   └── AttendanceRepository.java
│       │           ├── model/
│       │           │   ├── Coach.java
│       │           │   ├── Team.java
│       │           │   ├── Player.java
│       │           │   ├── Event.java
│       │           │   ├── Attendance.java
│       │           │   ├── Role.java                  # Enum: ADMIN, COACH
│       │           │   └── EventType.java             # Enum: TRAINING, MATCH
│       │           └── dto/
│       │               ├── PlayerStatsDto.java
│       │               ├── TeamStatsDto.java
│       │               └── AttendanceGridDto.java
│       └── resources/
│           ├── templates/
│           │   ├── layout/
│           │   │   ├── base.html                      # Base layout (Thymeleaf fragment)
│           │   │   └── navbar.html                    # Navigation bar fragment
│           │   ├── auth/
│           │   │   └── login.html
│           │   ├── dashboard/
│           │   │   └── index.html                     # Home page after login
│           │   ├── teams/
│           │   │   ├── list.html                      # List of teams for current coach
│           │   │   ├── detail.html                    # Team detail with attendance grid
│           │   │   ├── create.html
│           │   │   └── edit.html
│           │   ├── players/
│           │   │   ├── create.html
│           │   │   └── edit.html
│           │   ├── events/
│           │   │   ├── create.html
│           │   │   └── edit.html
│           │   ├── attendance/
│           │   │   └── grid.html                      # Attendance registration grid
│           │   └── admin/
│           │       ├── dashboard.html                 # Admin overview
│           │       ├── coaches.html                   # Manage all coaches
│           │       └── all-teams.html                 # View all teams
│           ├── static/
│           │   ├── css/
│           │   │   └── app.css                        # Custom styles on top of Bootstrap
│           │   └── js/
│           │       └── app.js                         # Minimal JS (attendance toggle, etc.)
│           ├── messages_en.properties                 # Default language (English)
│           ├── messages_it.properties                 # Italian translation
│           └── application.properties                 # Main config
├── docker-compose.yml
├── Dockerfile
├── .env.example                                       # Template for environment variables
├── pom.xml
└── CLAUDE.md                                          # This file
```

---

## Database Schema

### Entity: Coach

```java
@Entity
@Table(name = "coaches")
public class Coach {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;          // BCrypt hashed

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;               // ADMIN or COACH

    @Column(nullable = false)
    private boolean enabled = true;

    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL)
    private List<Team> teams;
}
```

### Entity: Team

```java
@Entity
@Table(name = "teams")
public class Team {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String season;            // e.g. "2024-2025"

    @ManyToOne(optional = false)
    @JoinColumn(name = "coach_id")
    private Coach coach;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Player> players;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("date ASC")
    private List<Event> events;
}
```

### Entity: Player

```java
@Entity
@Table(name = "players")
public class Player {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @ManyToOne(optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendances;
}
```

### Entity: Event

```java
@Entity
@Table(name = "events")
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;           // TRAINING or MATCH

    @Column(nullable = false)
    private LocalDate date;

    private String notes;

    @ManyToOne(optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendances;
}
```

### Entity: Attendance

```java
@Entity
@Table(name = "attendances",
       uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "event_id"}))
public class Attendance {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(nullable = false)
    private boolean present;          // true = PRESENT, false = ABSENT
}
```

### Enums

```java
public enum Role { ADMIN, COACH }
public enum EventType { TRAINING, MATCH }
```

---

## Security Configuration

Use Spring Security with form login.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/login", "/error").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**Authorization rules**:
- `/admin/**` — only ADMIN role
- All other authenticated routes — ADMIN or COACH
- A coach can ONLY access their own teams (enforce this in the service layer, not just security config)
- ADMIN can access any team

**Service-layer ownership check** (example for TeamService):
```java
public Team getTeamForCurrentCoach(Long teamId) {
    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new EntityNotFoundException("Team not found"));

    Coach currentCoach = getCurrentAuthenticatedCoach();
    if (currentCoach.getRole() == Role.ADMIN) return team; // Admin bypass

    if (!team.getCoach().getId().equals(currentCoach.getId())) {
        throw new AccessDeniedException("You do not have access to this team");
    }
    return team;
}
```

---

## Internationalization (i18n)

### Configuration

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
```

### Language switch

A link in the navbar with `?lang=it` or `?lang=en` switches the language and stores it
in the session. No page rebuild required.

```html
<!-- In navbar.html -->
<a th:href="@{''(lang=en)}">EN</a> |
<a th:href="@{''(lang=it)}">IT</a>
```

### messages_en.properties (excerpt — English is the default and official language)

```properties
# Navigation
nav.dashboard=Dashboard
nav.teams=Teams
nav.logout=Logout
nav.admin=Admin Panel

# Team
team.name=Team Name
team.season=Season
team.create=Create Team
team.edit=Edit Team
team.delete=Delete Team
team.players=Players
team.events=Events

# Player
player.firstName=First Name
player.lastName=Last Name
player.add=Add Player
player.remove=Remove Player

# Event
event.type=Type
event.type.training=Training
event.type.match=Match
event.date=Date
event.notes=Notes
event.add=Add Event

# Attendance
attendance.present=Present
attendance.absent=Absent
attendance.save=Save Attendance

# Statistics
stats.trainingAttendance=Training Attendance
stats.matchAttendance=Match Attendance
stats.trainingPercentage=Training %
stats.matchPercentage=Match %
stats.lowAttendanceWarning=Low attendance in last 3 trainings
stats.top3trainings=Top 3 (Trainings)
stats.top3matches=Top 3 (Matches)

# Admin
admin.coaches=Coaches
admin.allTeams=All Teams
admin.createCoach=Create Coach Account
admin.disableCoach=Disable Coach

# Auth
auth.login=Login
auth.username=Username
auth.password=Password
auth.logout=Logout
auth.loginError=Invalid username or password.
```

### messages_it.properties (excerpt — Italian translation)

```properties
# Navigation
nav.dashboard=Dashboard
nav.teams=Squadre
nav.logout=Esci
nav.admin=Pannello Admin

# Team
team.name=Nome Squadra
team.season=Stagione
team.create=Crea Squadra
team.edit=Modifica Squadra
team.delete=Elimina Squadra
team.players=Giocatori
team.events=Eventi

# Player
player.firstName=Nome
player.lastName=Cognome
player.add=Aggiungi Giocatore
player.remove=Rimuovi Giocatore

# Event
event.type=Tipo
event.type.training=Allenamento
event.type.match=Partita
event.date=Data
event.notes=Note
event.add=Aggiungi Evento

# Attendance
attendance.present=Presente
attendance.absent=Assente
attendance.save=Salva Presenze

# Statistics
stats.trainingAttendance=Presenze Allenamenti
stats.matchAttendance=Presenze Partite
stats.trainingPercentage=% Allenamenti
stats.matchPercentage=% Partite
stats.lowAttendanceWarning=Poche presenze negli ultimi 3 allenamenti
stats.top3trainings=Top 3 (Allenamenti)
stats.top3matches=Top 3 (Partite)

# Admin
admin.coaches=Allenatori
admin.allTeams=Tutte le Squadre
admin.createCoach=Crea Account Allenatore
admin.disableCoach=Disabilita Allenatore

# Auth
auth.login=Accedi
auth.username=Username
auth.password=Password
auth.logout=Esci
auth.loginError=Username o password non validi.
```

---

## Core Business Logic

### Statistics Calculation (StatisticsService)

For each player in a team, compute:

1. **Training attendance**: count of events with `type = TRAINING` where `attendance.present = true` / total TRAINING events for the team. Format: "X / Y"
2. **Match attendance**: same for `type = MATCH`. Format: "X / Y"
3. **Training percentage**: (trainings present / total trainings) * 100. Format: "XX%"
4. **Match percentage**: same for matches.
5. **Low attendance flag**: if the player attended at most 1 of the last 3 TRAINING events (only applied when there are at least 3 training events total). Highlight the player row in red.
6. **Top 3 players**: rank players by training attendance count (descending) and by match attendance count (descending). Show top 3 for each.

### Attendance Grid (AttendanceGridDto)

The main view of a team shows a grid similar to the spreadsheet:

- Rows = players
- Columns = events (ordered by date ascending)
- Each cell = present/absent toggle for that player+event combination
- The grid is rendered server-side with Thymeleaf
- JavaScript handles the toggle (click to switch P/A) and submits via AJAX or form POST

When a new event is added, generate `Attendance` records for all current players with `present = false` as default.

When a new player is added to a team that already has events, generate `Attendance` records for all existing events with `present = false` as default.

### Season Management

A team belongs to a season (string field, e.g. "2024-2025"). To start a new season:
- Create a new team with the same name and a new season string
- Optionally copy the player roster to the new team
- Old season data is preserved (read-only archive viewable from the team list)

---

## UI / UX Guidelines

### General principles
- **Modern and clean**: use Bootstrap 5 components consistently
- **Intuitive**: a coach should be able to use the app without reading a manual
- **Responsive**: works on desktop and tablet (mobile is not a priority)
- **Feedback**: always show success/error flash messages after actions (use Spring's RedirectAttributes)

### Color scheme (app.css)
```css
:root {
    --color-primary: #1e3a5f;       /* Dark navy - main brand color */
    --color-secondary: #2d6a4f;     /* Forest green - secondary actions */
    --color-training: #d4e6f1;      /* Light blue - training event columns */
    --color-match: #fde8d8;         /* Light orange - match event columns */
    --color-present: #d5f5e3;       /* Light green - present cell */
    --color-absent: #fadbd8;        /* Light red - absent cell */
    --color-warning: #ffeeba;       /* Yellow - low attendance warning */
    --color-text: #212529;
    --color-muted: #6c757d;
}
```

### Attendance grid styling
- Training event columns: light blue header (`--color-training`)
- Match event columns: light orange header (`--color-match`)
- Present cell: light green background (`--color-present`)
- Absent cell: light red background (`--color-absent`)
- Player row with low attendance: yellow background (`--color-warning`) on the player name cell
- Statistics columns: separated visually from event columns (different background, bold)

### Flash messages
Use Bootstrap alerts with auto-dismiss (via JS, 4 seconds):
```html
<!-- In base layout, check for flash attributes -->
<div th:if="${successMessage}" class="alert alert-success alert-dismissible" role="alert">
    <span th:text="${successMessage}"></span>
</div>
<div th:if="${errorMessage}" class="alert alert-danger alert-dismissible" role="alert">
    <span th:text="${errorMessage}"></span>
</div>
```

### Navigation structure
- **Navbar** (all authenticated users):
  - Logo / App name ("Attendance Manager")
  - Teams (dropdown if multiple)
  - Language switcher (EN | IT)
  - User name + Logout
- **Navbar** (admin only, additional item):
  - Admin Panel

---

## API / Controller Conventions

Use standard Spring MVC with Thymeleaf (no REST API needed for the initial version).

| Route | Method | Controller | Description |
|---|---|---|---|
| `/login` | GET/POST | AuthController | Login page |
| `/dashboard` | GET | DashboardController | Home after login |
| `/teams` | GET | TeamController | List coach's teams |
| `/teams/create` | GET/POST | TeamController | Create new team |
| `/teams/{id}` | GET | TeamController | Team detail + attendance grid |
| `/teams/{id}/edit` | GET/POST | TeamController | Edit team |
| `/teams/{id}/delete` | POST | TeamController | Delete team |
| `/teams/{id}/players/add` | POST | PlayerController | Add player to team |
| `/teams/{id}/players/{pid}/remove` | POST | PlayerController | Remove player |
| `/teams/{id}/events/add` | POST | EventController | Add event to team |
| `/teams/{id}/events/{eid}/delete` | POST | EventController | Delete event |
| `/teams/{id}/attendance` | POST | AttendanceController | Save attendance grid |
| `/language` | GET | LanguageController | Switch language (uses ?lang= param) |
| `/admin` | GET | AdminController | Admin dashboard |
| `/admin/coaches` | GET | AdminController | List all coaches |
| `/admin/coaches/create` | GET/POST | AdminController | Create coach account |
| `/admin/coaches/{id}/toggle` | POST | AdminController | Enable/disable coach |
| `/admin/teams` | GET | AdminController | View all teams |

Use POST + redirect pattern (PRG - Post/Redirect/Get) for all form submissions to avoid
duplicate submissions on browser refresh.

---

## application.properties

```properties
# Server
server.port=8080

# Database (values come from .env via docker-compose)
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:attendance_db}
spring.datasource.username=${DB_USER:attendance_user}
spring.datasource.password=${DB_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Thymeleaf
spring.thymeleaf.cache=false

# i18n
spring.messages.basename=messages
spring.messages.encoding=UTF-8

# Admin seed (used by DataInitializer)
app.admin.username=${ADMIN_USERNAME:admin}
app.admin.password=${ADMIN_PASSWORD}
app.admin.fullname=${ADMIN_FULLNAME:Administrator}
```

---

## Docker Setup

### Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/attendance-manager-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml

```yaml
version: '3.8'

services:
  db:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      DB_HOST: db
      DB_PORT: 5432
      DB_NAME: ${DB_NAME}
      DB_USER: ${DB_USER}
      DB_PASSWORD: ${DB_PASSWORD}
      ADMIN_USERNAME: ${ADMIN_USERNAME}
      ADMIN_PASSWORD: ${ADMIN_PASSWORD}
      ADMIN_FULLNAME: ${ADMIN_FULLNAME}
    depends_on:
      - db

volumes:
  postgres_data:
```

### .env.example

```env
DB_NAME=attendance_db
DB_USER=attendance_user
DB_PASSWORD=change_me_in_production

ADMIN_USERNAME=admin
ADMIN_PASSWORD=change_me_in_production
ADMIN_FULLNAME=Lorenzo
```

---

## Admin Seed (DataInitializer)

On application startup, check if the admin account exists. If not, create it using the
environment variables.

```java
@Component
public class DataInitializer implements CommandLineRunner {

    private final CoachRepository coachRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.fullname}")
    private String adminFullName;

    @Override
    public void run(String... args) {
        if (coachRepository.findByUsername(adminUsername).isEmpty()) {
            Coach admin = new Coach();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setFullName(adminFullName);
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            coachRepository.save(admin);
        }
    }
}
```

---

## pom.xml Dependencies

```xml
<dependencies>
    <!-- Spring Boot starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Thymeleaf + Spring Security integration (th:sec:authorize etc.) -->
    <dependency>
        <groupId>org.thymeleaf.extras</groupId>
        <artifactId>thymeleaf-extras-springsecurity6</artifactId>
    </dependency>

    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok (optional, reduces boilerplate getters/setters) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## Coding Conventions

- **Language**: all code, comments, variable names, method names, class names in **English**
- **Style**: standard Java naming conventions (camelCase for methods/variables, PascalCase for classes)
- **Annotations**: use Spring annotations consistently (@Service, @Repository, @Controller, @Transactional)
- **Exception handling**: use a global `@ControllerAdvice` to handle common exceptions (EntityNotFoundException, AccessDeniedException) and redirect to appropriate error pages
- **No hardcoded strings in controllers**: use `messages_en.properties` keys via Thymeleaf `#{key}` syntax for all UI text
- **Lombok**: use @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor, @Builder on entities and DTOs to reduce boilerplate
- **DTOs**: never pass JPA entities directly to templates; use DTOs or DTO-like projections for complex views (especially the attendance grid)

---

## Local Setup and Git Workflow

### Step 0 — Project directory and local repository

Before writing any code, set up the project directory and initialize the local Git
repository. **All files are created locally first, then pushed to GitHub.**

```bash
# 1. Navigate to the directory chosen by the user (ask them if not specified)
cd /path/chosen/by/user

# 2. Create the project folder
mkdir attendance-manager
cd attendance-manager

# 3. Initialize local Git repository
git init

# 4. Create .gitignore immediately (before any other file)
cat > .gitignore << 'EOF'
# Maven
target/
*.class

# IDE
.idea/
*.iml
.vscode/
*.swp

# Environment — NEVER commit secrets
.env

# OS
.DS_Store
Thumbs.db
EOF

# 5. Create the .env.example file (safe to commit, no real secrets)
# Then add all project files...

# 6. First commit on main — project scaffold only
git add .
git commit -m "Initial project scaffold"

# 7. Add GitHub remote (user must create the empty repo on GitHub first)
git remote add origin https://github.com/<username>/attendance-manager.git

# 8. Push main branch
git push -u origin main
```

> **Important**: the `.env` file (with real passwords) must NEVER be committed.
> Only `.env.example` (with placeholder values) goes into the repository.
> Verify `.env` is in `.gitignore` before the first push.

---

### Branch strategy (lightweight, no Scrum overhead)

Two permanent rules:
- `main` is **always in a working state**. Never commit broken code directly to main.
- Each feature gets its own branch, created from `main`, merged back when complete.

**Branch naming convention**: `feature/<short-description>` using kebab-case.

```
main
 └── feature/authentication
 └── feature/team-management
 └── feature/player-management
 └── feature/event-management
 └── feature/attendance-grid
 └── feature/statistics
 └── feature/admin-panel
 └── feature/i18n
 └── feature/ui-polish
```

One branch per item in the Development Order section below. When a feature is complete
and tested locally, merge it into `main` and push both branches.

---

### Git commands for each feature (repeat for every step)

```bash
# --- START of a new feature ---

# Make sure you are on main and it is up to date
git checkout main
git pull origin main

# Create and switch to the feature branch
git branch feature/<name>
git checkout feature/<name>
# (or shorthand: git checkout -b feature/<name>)

# --- DURING development ---

# Stage and commit frequently as you complete small working pieces
git add .
git commit -m "<verb in imperative form> <what was done>"

# Examples of good commit messages:
#   Add Coach entity and repository
#   Configure Spring Security with form login
#   Add attendance toggle logic in grid view
#   Fix percentage calculation when no events exist

# --- END of feature (when tested and working) ---

# Merge back into main
git checkout main
git merge feature/<name>

# Push both branches to GitHub
git push origin main
git push origin feature/<name>
```

---

### Commit message rules

Format: `<Verb> <what>` — imperative, present tense, no period at end.

| Good | Bad |
|---|---|
| `Add Player entity and repository` | `added player` |
| `Fix statistics for teams with no events` | `bug fix` |
| `Configure BCrypt password encoder` | `security stuff` |
| `Add Italian translations for all labels` | `i18n` |

Commit after each meaningful, self-contained working piece — not after every single file
save, and not in one giant commit at the end of the day.

---

### GitHub Issues as a personal todo list

Create one Issue on GitHub for each item in the Development Order list before starting.
Title format: `[Feature] <name>` — e.g. `[Feature] Attendance grid`.

When starting a feature: assign the issue to yourself, move it to "In Progress".
When merging to main: close the issue with `Closes #<number>` in the merge commit message.

```bash
# Closing an issue via commit message (GitHub detects this automatically)
git commit -m "Add attendance grid with present/absent toggle - Closes #6"
```

This creates a clean history of what was built and when, with zero Scrum ceremony.

---

### Future Scrum migration (when ready)

The repository structure does not need to change. When you decide to work with Scrum:

1. Activate **GitHub Projects** and link the existing repository
2. Convert existing open Issues into Project board items
3. Start working in sprints: plan → feature branch → PR with review → merge to main
4. Add pull request reviews instead of direct merges
5. GitHub Actions can be added later for automated testing (CI/CD)

None of the existing code or history is affected. The process layer sits above the
repository, not inside it.

---

## Development Order (recommended)

Follow this sequence to build incrementally with a working app at each step:

1. **Project scaffold** — Spring Initializr, pom.xml, Docker, application.properties, DataInitializer, basic login page
2. **Authentication** — Login/logout, SecurityConfig, Coach entity, login page with error messages
3. **Team management** — Team entity, TeamController, team list and create/edit pages
4. **Player management** — Player entity, add/remove players from a team
5. **Event management** — Event entity (Training/Match), add/remove events from a team
6. **Attendance grid** — Attendance entity, grid view, toggle present/absent, save
7. **Statistics** — StatisticsService, stats columns in the grid, low attendance flag, top 3
8. **Admin panel** — AdminController, coach management, all-teams view
9. **Internationalization** — messages_it.properties, language switcher in navbar
10. **UI polish** — app.css colors, Bootstrap refinements, flash messages, loading states

---

## What This App Is NOT (keep it simple)

- No REST API (Thymeleaf server-side rendering is sufficient)
- No reactive / async programming
- No email sending
- No file upload / import from CSV (can be added later)
- No real-time updates (page refresh is acceptable)
- No mobile-first (desktop/tablet is the target)
- No external authentication (OAuth, SSO) — simple username/password is enough
- No payment system

If asked to add any of these, defer to a future phase unless explicitly required.