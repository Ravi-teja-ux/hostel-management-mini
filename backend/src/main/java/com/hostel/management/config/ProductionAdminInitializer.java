package com.hostel.management.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import com.hostel.management.entity.Admin;
import com.hostel.management.repository.AdminRepository;

@Configuration
@Profile("prod")
public class ProductionAdminInitializer {

    @Bean
    ApplicationRunner initializeProductionAdmin(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminId = System.getenv("ADMIN_INITIAL_ID");
            String password = System.getenv("ADMIN_INITIAL_PASSWORD");
            String jwtSecret = System.getenv("JWT_SECRET");
            if (!StringUtils.hasText(adminId) || !StringUtils.hasText(password) || password.length() < 14) {
                throw new IllegalStateException(
                        "Set ADMIN_INITIAL_ID and an ADMIN_INITIAL_PASSWORD of at least 14 characters.");
            }
            if (!StringUtils.hasText(jwtSecret) || jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
                throw new IllegalStateException("Set a randomly generated JWT_SECRET of at least 32 bytes.");
            }
            if (adminId.length() > 30) {
                throw new IllegalStateException("ADMIN_INITIAL_ID must be 30 characters or fewer.");
            }
            Admin admin = adminRepository.findByAdminId(adminId).orElseGet(() -> new Admin(
                    adminId,
                    passwordEncoder.encode(password),
                    "Hostel Administrator"));
            if (!passwordEncoder.matches(password, admin.getPassword())) {
                admin.setPassword(passwordEncoder.encode(password));
            }
            adminRepository.save(admin);
        };
    }
}
