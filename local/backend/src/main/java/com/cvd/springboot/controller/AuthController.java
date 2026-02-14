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
    
    private final OtpService otpService;
    private final JwtUtil jwtUtil;
    private final PatientLoginRepository patientLoginRepository;
    
    public AuthController(OtpService otpService, JwtUtil jwtUtil, 
                         PatientLoginRepository patientLoginRepository) {
        this.otpService = otpService;
        this.jwtUtil = jwtUtil;
        this.patientLoginRepository = patientLoginRepository;
    }
    
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
        String otp = String.valueOf(request.get("otp"));
        String name = request.get("name") != null ? String.valueOf(request.get("name")) : null;
        
        System.out.println("📥 Verify OTP - Mobile: " + mobile + ", OTP: " + otp + ", Name: " + name);
        
        // Check if user exists FIRST (before verifying OTP)
        Optional<PatientLogin> existingLogin = patientLoginRepository.findByPhone(mobile);
        boolean isNewUser = existingLogin.isEmpty();
        
        // If new user, require name BEFORE verifying OTP
        if (isNewUser && (name == null || name.trim().isEmpty() || name.equals("null"))) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Name is required for new registration",
                    "isNewUser", true
                ));
        }
        
        // NOW verify OTP (after all validations)
        boolean valid = otpService.verifyOtp(mobile, otp);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid OTP"));
        }
        
        PatientLogin login;
        
        if (existingLogin.isPresent()) {
            // Existing user - update last login
            login = existingLogin.get();
            login.setLastLogin(LocalDateTime.now());
            patientLoginRepository.save(login);
        } else {
            // New user - create login entry with auto-generated CVD ID
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
            "message", isNewUser ? "Registration successful" : "Login successful",
            "token", token,
            "patientId", login.getPatientId(),
            "name", login.getName(),
            "isNewUser", isNewUser
        ));
    }
    
    /**
     * Auto-generates patient ID (CVD0001, CVD0002, etc.)
     */
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