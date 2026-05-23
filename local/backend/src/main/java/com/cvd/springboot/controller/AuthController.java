package com.cvd.springboot.controller;

import com.cvd.springboot.entity.Admin;
import com.cvd.springboot.entity.PatientLogin;
import com.cvd.springboot.repository.AdminRepository;
import com.cvd.springboot.repository.PatientLoginRepository;
import com.cvd.springboot.security.JwtUtil;
import com.cvd.springboot.service.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    /**
     * Secret key required for self-registration of new admin accounts.
     * Anyone who knows this key can create an admin account.
     * Only share this with trusted personnel.
     */
    private static final String ADMIN_SECRET_KEY = "CVD11166";

    private final OtpService             otpService;
    private final JwtUtil                jwtUtil;
    private final PatientLoginRepository patientLoginRepository;
    private final AdminRepository        adminRepository;
    private final PasswordEncoder        passwordEncoder;

    public AuthController(OtpService otpService,
                          JwtUtil jwtUtil,
                          PatientLoginRepository patientLoginRepository,
                          AdminRepository adminRepository,
                          PasswordEncoder passwordEncoder) {
        this.otpService             = otpService;
        this.jwtUtil                = jwtUtil;
        this.patientLoginRepository = patientLoginRepository;
        this.adminRepository        = adminRepository;
        this.passwordEncoder        = passwordEncoder;
    }

    // ════════════════════════════════════════════════════════
    // ADMIN LOGIN — queries DB, BCrypt comparison
    // ════════════════════════════════════════════════════════
    @PostMapping("/admin-login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> request) {
        String reqAdminId = request.get("adminId");
        String password   = request.get("password");

        if (reqAdminId == null || password == null ||
            reqAdminId.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Admin ID and password are required"));
        }

        Optional<Admin> adminOpt = adminRepository.findByAdminId(reqAdminId.trim().toUpperCase());
        if (adminOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid admin credentials"));
        }

        Admin admin = adminOpt.get();

        if (!passwordEncoder.matches(password.trim(), admin.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid admin credentials"));
        }

        String token = jwtUtil.generateToken("ADMIN:" + admin.getAdminId());

        return ResponseEntity.ok(Map.of(
                "message", "Admin login successful",
                "token",   token,
                "role",    admin.getRole(),
                "adminId", admin.getAdminId(),
                "name",    admin.getName()
        ));
    }

    // ════════════════════════════════════════════════════════
    // ADMIN SELF-REGISTRATION
    //
    // Public endpoint — no JWT required.
    // Anyone can create an admin account IF they know the secret key.
    //
    // Body: { "secretKey": "CVD11166", "name": "...",
    //         "adminId": "...", "password": "..." }
    //
    // Flow:
    //   1. Validate secret key matches CVD11166
    //   2. Validate fields
    //   3. BCrypt-hash the password
    //   4. Save to admins table
    //   5. Return success (password hash never returned)
    // ════════════════════════════════════════════════════════
    @PostMapping("/admin-register")
    @Transactional
    public ResponseEntity<?> adminRegister(@RequestBody Map<String, String> body) {
        String secretKey = body.get("secretKey");
        String name      = body.get("name");
        String adminId   = body.get("adminId");
        String password  = body.get("password");

        // ── 1. Validate secret key ────────────────────────
        if (secretKey == null || !ADMIN_SECRET_KEY.equals(secretKey.trim())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Invalid secret key. Access denied."));
        }

        // ── 2. Validate fields ────────────────────────────
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        }
        if (adminId == null || adminId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Admin ID is required"));
        }
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
        }
        if (password.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Password must be at least 6 characters"));
        }

        String normalizedId = adminId.trim().toUpperCase();

        if (adminRepository.existsByAdminId(normalizedId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Admin ID already exists: " + normalizedId));
        }

        // ── 3 + 4. Hash password and save ────────────────
        Admin newAdmin = new Admin();
        newAdmin.setAdminId(normalizedId);
        newAdmin.setName(name.trim());
        newAdmin.setPassword(passwordEncoder.encode(password.trim())); // BCrypt hash
        newAdmin.setRole("ADMIN");
        newAdmin.setCreatedAt(LocalDateTime.now());

        adminRepository.save(newAdmin);

        // ── 5. Return success — password NEVER in response ─
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Admin account created successfully. You can now log in.",
                "adminId", normalizedId,
                "name",    newAdmin.getName(),
                "role",    newAdmin.getRole()
        ));
    }

    // ════════════════════════════════════════════════════════
    // PATIENT OTP FLOW
    // ════════════════════════════════════════════════════════
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String mobile = request.get("mobile");
        if (mobile == null || mobile.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Mobile number is required"));
        }
        otpService.generateOtp(mobile);
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
    }

    @PostMapping("/verify-otp")
    @Transactional
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, Object> request) {
        String mobile = String.valueOf(request.get("mobile"));
        String otp    = String.valueOf(request.get("otp"));
        String name   = request.get("name") != null
                        ? String.valueOf(request.get("name")) : null;

        Optional<PatientLogin> existingLogin = patientLoginRepository.findByPhone(mobile);
        boolean isNewUser = existingLogin.isEmpty();

        if (isNewUser && (name == null || name.trim().isEmpty() || name.equals("null"))) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name is required for new registration",
                                 "isNewUser", true));
        }

        if (!otpService.verifyOtp(mobile, otp)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid OTP"));
        }

        PatientLogin login;
        if (existingLogin.isPresent()) {
            login = existingLogin.get();
            login.setLastLogin(LocalDateTime.now());
            patientLoginRepository.save(login);
        } else {
            login = new PatientLogin();
            login.setPatientId(generatePatientId());
            login.setName(name);
            login.setPhone(mobile);
            login.setCreatedAt(LocalDateTime.now());
            login.setLastLogin(LocalDateTime.now());
            login = patientLoginRepository.save(login);
        }

        String token = jwtUtil.generateToken(mobile);
        return ResponseEntity.ok(Map.of(
                "message",   isNewUser ? "Registration successful" : "Login successful",
                "token",     token,
                "patientId", login.getPatientId(),
                "name",      login.getName(),
                "isNewUser", isNewUser
        ));
    }

    @GetMapping("/logins")
    public ResponseEntity<?> getAllLogins() {
        return ResponseEntity.ok(patientLoginRepository.findAll());
    }

    private String generatePatientId() {
        Long maxSlNo = patientLoginRepository.findMaxSlNo();
        int next = (maxSlNo == null) ? 1 : maxSlNo.intValue() + 1;
        return String.format("CVD%04d", next);
    }
}
