package com.cvd.springboot.controller;

import com.cvd.springboot.entity.PatientLogin;
import com.cvd.springboot.repository.PatientLoginRepository;
import com.cvd.springboot.security.JwtUtil;
import com.cvd.springboot.service.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    // ── Single hardcoded admin credentials ──────────
    private static final String ADMIN_ID       = "ADMCVD2026";
    private static final String ADMIN_PASSWORD = "ADMIN@2026";

    private final OtpService otpService;
    private final JwtUtil jwtUtil;
    private final PatientLoginRepository patientLoginRepository;

    public AuthController(OtpService otpService, JwtUtil jwtUtil,
                          PatientLoginRepository patientLoginRepository) {
        this.otpService = otpService;
        this.jwtUtil = jwtUtil;
        this.patientLoginRepository = patientLoginRepository;
    }

    // ════════════════════════════════════════════════
    // ADMIN LOGIN  — POST /api/auth/admin-login
    // ════════════════════════════════════════════════
    @PostMapping("/admin-login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> request) {
        String adminId   = request.get("adminId");
        String password  = request.get("password");

        if (adminId == null || password == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Admin ID and password are required"));
        }

        if (!ADMIN_ID.equals(adminId.trim()) || !ADMIN_PASSWORD.equals(password.trim())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid admin credentials"));
        }

        // Generate token with subject "ADMIN"
        String token = jwtUtil.generateToken("ADMIN:" + ADMIN_ID);

        return ResponseEntity.ok(Map.of(
                "message", "Admin login successful",
                "token",   token,
                "role",    "ADMIN",
                "adminId", ADMIN_ID
        ));
    }

    // ════════════════════════════════════════════════
    // PATIENT OTP FLOW
    // ════════════════════════════════════════════════
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
        String name   = request.get("name") != null ? String.valueOf(request.get("name")) : null;

        Optional<PatientLogin> existingLogin = patientLoginRepository.findByPhone(mobile);
        boolean isNewUser = existingLogin.isEmpty();

        if (isNewUser && (name == null || name.trim().isEmpty() || name.equals("null"))) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name is required for new registration", "isNewUser", true));
        }

        boolean valid = otpService.verifyOtp(mobile, otp);
        if (!valid) {
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

    private String generatePatientId() {
        Long maxSlNo = patientLoginRepository.findMaxSlNo();
        int nextNumber = (maxSlNo == null) ? 1 : maxSlNo.intValue() + 1;
        return String.format("CVD%04d", nextNumber);
    }

    @GetMapping("/logins")
    public ResponseEntity<?> getAllLogins() {
        return ResponseEntity.ok(patientLoginRepository.findAll());
    }
}
