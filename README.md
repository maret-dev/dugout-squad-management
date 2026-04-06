# Dugout

Web application for managing player attendance at training sessions and matches for sports clubs. Replaces a Google Sheets + Apps Script system with a proper web app that coaches can use from any browser.

## Features

- **Team Management** — Create and manage multiple teams with season tracking
- **Player Management** — Add/remove players from teams
- **Event Management** — Track trainings and matches with dates and notes
- **Attendance Grid** — Interactive grid to mark players as present/absent with one click
- **Statistics** — Automatic calculation of attendance percentages, low attendance warnings, top 3 rankings
- **Admin Panel** — Manage coach accounts, enable/disable coaches, view all teams
- **Multi-language** — English (default) and Italian, switchable from the navbar
- **Authentication** — Role-based access control (ADMIN / COACH) with Spring Security

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.x |
| ORM | Spring Data JPA + Hibernate |
| Security | Spring Security (form login, BCrypt) |
| Database | PostgreSQL 15 |
| Templates | Thymeleaf |
| CSS | Bootstrap 5.3 |
| Build | Maven |
| Container | Docker + Docker Compose |

## Prerequisites

- **Docker** and **Docker Compose** installed ([Get Docker](https://docs.docker.com/get-docker/))
- **Git** installed

That's it — no Java or Maven installation needed, Docker handles everything.

## Quick Start

### 1. Clone the repository

```bash
git clone git@github.com:maret-dev/dugout-squad-management.git
cd dugout-squad-management
```

### 2. Configure environment variables

```bash
cp .env.example .env
```

Edit `.env` with your values:

```env
DB_NAME=attendance_db
DB_USER=attendance_user
DB_PASSWORD=your_secure_password_here

ADMIN_USERNAME=admin
ADMIN_PASSWORD=your_admin_password_here
ADMIN_FULLNAME=Your Name
```

> **Important**: Never commit the `.env` file. It is already in `.gitignore`.

### 3. Build and run with Docker Compose

```bash
# Build the Java application first (requires Maven, or use the Docker multi-stage approach below)
# If you have Maven installed locally:
mvn clean package -DskipTests

# Then start everything:
docker compose up --build
```

If you don't have Maven installed locally, you can use a multi-stage Docker build by replacing the `Dockerfile` content (see [Development Setup](#development-setup-without-docker) below).

### 4. Open the application

Navigate to **http://localhost:8080** in your browser.

Log in with the admin credentials you set in `.env`.

## Development Setup (without Docker)

If you prefer to run the application directly for development:

### Prerequisites

- Java 17 (JDK)
- Maven 3.9+
- PostgreSQL 15 running locally

### Steps

1. **Start PostgreSQL** and create the database:

```sql
CREATE DATABASE attendance_db;
CREATE USER attendance_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE attendance_db TO attendance_user;
```

2. **Set environment variables** (or edit `application.properties`):

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=attendance_db
export DB_USER=attendance_user
export DB_PASSWORD=your_password
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=your_admin_password
export ADMIN_FULLNAME=YourName
```

3. **Run the application**:

```bash
mvn spring-boot:run
```

The app starts at **http://localhost:8080**.

## Usage Guide

### First Login

After starting the application, an admin account is automatically created using the credentials from your `.env` file. Go to `http://localhost:8080` and log in.

### Managing Teams

1. Click **Teams** in the navbar
2. Click **Create Team** — enter the team name and season (e.g. "2024-2025")
3. Click a team name to open the team detail page

### Adding Players

On the team detail page:
1. Scroll to the **Players** section
2. Enter the player's first name and last name
3. Click **Add Player**

Players can be removed with the **Remove** button next to their name.

### Adding Events

On the team detail page:
1. Scroll to the **Events** section
2. Select the event type (Training or Match)
3. Pick a date and optionally add notes
4. Click **Add Event**

When you add an event, attendance records are automatically created for all current players (defaulting to "Absent").

When you add a new player, attendance records are automatically created for all existing events.

### Recording Attendance

The **attendance grid** appears at the top of the team detail page once you have both players and events:

- **Rows** = players, **Columns** = events (ordered by date)
- Click a cell to toggle between **P** (Present, green) and **A** (Absent, red)
- Column headers are color-coded: light blue = Training, light orange = Match
- Click **Save Attendance** to persist your changes

### Reading Statistics

Statistics columns appear to the right of the attendance grid:

| Column | Meaning |
|---|---|
| Training | Present count / Total trainings |
| Training % | Attendance percentage for trainings |
| Match | Present count / Total matches |
| Match % | Attendance percentage for matches |

- Players with **low attendance** (attended 1 or fewer of the last 3 trainings) are highlighted in yellow
- **Top 3** cards below the grid show the best-attending players for trainings and matches

### Admin Panel

Accessible only to users with the ADMIN role:

1. Click **Admin Panel** in the navbar
2. **Coaches** — view all coach accounts, create new ones, enable/disable existing ones
3. **All Teams** — view all teams across all coaches

When creating a coach, assign them the COACH or ADMIN role. Disabled coaches cannot log in.

### Switching Language

Click **EN** or **IT** in the navbar to switch between English and Italian. The language is stored in the session and persists across pages.

### Season Management

To start a new season:
1. Create a new team with the same name but a different season string (e.g. "2025-2026")
2. Re-add the players you want to carry over
3. The old season's team and data remain accessible as a read-only archive

## Project Structure

```
src/main/java/com/dugout/
├── config/          # SecurityConfig, WebMvcConfig, DataInitializer, GlobalExceptionHandler
├── controller/      # Auth, Dashboard, Team, Player, Event, Attendance, Admin controllers
├── service/         # CoachService, TeamService, PlayerService, EventService, AttendanceService, StatisticsService
├── repository/      # JPA repositories for all entities
├── model/           # JPA entities (Coach, Team, Player, Event, Attendance) + enums
└── dto/             # AttendanceGridDto, PlayerStatsDto, TeamStatsDto

src/main/resources/
├── templates/       # Thymeleaf templates (layout, auth, dashboard, teams, admin, error)
├── static/          # CSS (app.css) and JS (app.js)
├── application.properties
├── messages_en.properties
└── messages_it.properties
```

## Security

- All passwords are hashed with **BCrypt**
- Coaches can only access their own teams (enforced at the service layer)
- Admin users can access all teams
- `/admin/**` routes require ADMIN role
- CSRF protection enabled by default (Spring Security)

## License

Private project.
