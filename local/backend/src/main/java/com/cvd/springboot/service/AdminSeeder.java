package com.cvd.springboot.service;

import com.cvd.springboot.entity.Admin;
import com.cvd.springboot.repository.AdminRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * AdminSeeder — seeds the original super-admin on first startup.
 *
 * Super-admin credentials (as requested):
 *   Admin ID : ADMCVD2026
 *   Password : ADMIN@2026
 *   Role     : SUPER_ADMIN
 *
 * After startup you will see in psql:
 *   SELECT admin_id, name, password, role FROM admins;
 *
 *   admin_id   | name        | password              | role
 *   -----------+-------------+-----------------------+------------
 *   ADMCVD2026 | Super Admin | $2a$12$...            | SUPER_ADMIN
 *
 * New admins can self-register via POST /api/auth/admin-register
 * but MUST provide the secret key: CVD11166
 * Their passwords are also BCrypt-hashed before storage.
 */
@Component
public class AdminSeeder implements ApplicationRunner {

    // ── Super-admin credentials ────────────────────────────
    private static final String SUPER_ID       = "ADMCVD2026";
    private static final String SUPER_NAME     = "Super Admin";
    private static final String SUPER_PASSWORD = "ADMIN@2026";
    private static final String SUPER_ROLE     = "SUPER_ADMIN";

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(AdminRepository adminRepository,
                       PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder  = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Remove any stale bootstrap row from old seeder (ADMCVD001)
        adminRepository.findByAdminId("ADMCVD001").ifPresent(adminRepository::delete);

        // Seed the real super-admin — idempotent
        if (adminRepository.existsByAdminId(SUPER_ID)) {
            return;
        }

        Admin superAdmin = new Admin();
        superAdmin.setAdminId(SUPER_ID);
        superAdmin.setName(SUPER_NAME);
        superAdmin.setPassword(passwordEncoder.encode(SUPER_PASSWORD));
        superAdmin.setRole(SUPER_ROLE);

        adminRepository.save(superAdmin);

        System.out.println("""
                ╔══════════════════════════════════════════════════════════════╗
                ║                 SUPER ADMIN SEEDED                          ║
                ║  Admin ID : ADMCVD2026                                      ║
                ║  Password : ADMIN@2026  (BCrypt-hashed in `admins` table)   ║
                ║  Role     : SUPER_ADMIN                                     ║
                ║                                                              ║
                ║  New admins self-register at:                               ║
                ║  POST /api/auth/admin-register                              ║
                ║  with secret key: CVD11166                                  ║
                ╚══════════════════════════════════════════════════════════════╝
                """);
    }
}
