package com.dugout.config;

import com.dugout.model.Coach;
import com.dugout.model.Role;
import com.dugout.repository.CoachRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
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
            Coach admin = Coach.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .fullName(adminFullName)
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            coachRepository.save(admin);
        }
    }
}
