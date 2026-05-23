package com.cvd.springboot.controller;

import com.cvd.springboot.entity.Admin;
import com.cvd.springboot.repository.AdminRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AdminController — list and delete admin accounts.
 * Creation happens via POST /api/auth/admin-register (with secret key).
 *
 * All endpoints here require a valid JWT.
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    // Super-admin is permanently protected — can never be deleted
    private static final String SUPER_ADMIN_ID = "ADMCVD2026";

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(AdminRepository adminRepository,
                           PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder  = passwordEncoder;
    }

    // ════════════════════════════════════════════════════════
    // GET /api/admin/all — list all admins, passwords excluded
    // ════════════════════════════════════════════════════════
    @GetMapping("/all")
    public ResponseEntity<?> getAllAdmins() {
        List<Map<String, Object>> admins = adminRepository.findAll()
                .stream()
                .map(this::safeAdminView)
                .toList();
        return ResponseEntity.ok(admins);
    }

    // ════════════════════════════════════════════════════════
    // DELETE /api/admin/{adminId}
    // Super-admin ADMCVD2026 is permanently protected.
    // ════════════════════════════════════════════════════════
    @DeleteMapping("/{adminId}")
    @Transactional
    public ResponseEntity<?> deleteAdmin(@PathVariable String adminId) {
        if (SUPER_ADMIN_ID.equalsIgnoreCase(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Cannot delete the super-admin account (ADMCVD2026)"));
        }

        Optional<Admin> adminOpt = adminRepository.findByAdminId(adminId.toUpperCase());
        if (adminOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Admin not found: " + adminId));
        }

        adminRepository.delete(adminOpt.get());
        return ResponseEntity.ok(
                Map.of("message", "Admin " + adminId.toUpperCase() + " deleted successfully"));
    }

    // ── Never expose the password hash ──────────────────────
    private Map<String, Object> safeAdminView(Admin admin) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("adminId",   admin.getAdminId());
        m.put("name",      admin.getName());
        m.put("role",      admin.getRole());
        m.put("createdAt", admin.getCreatedAt());
        // password intentionally omitted from all API responses
        return m;
    }
}
