package com.cvd.springboot.controller;

import com.cvd.springboot.entity.Patient;
import com.cvd.springboot.entity.PatientLogin;
import com.cvd.springboot.repository.PatientLoginRepository;
import com.cvd.springboot.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "http://localhost:3000")
public class PatientController {
    
    private final PatientRepository patientRepository;
    private final PatientLoginRepository patientLoginRepository;
    
    public PatientController(PatientRepository patientRepository,
                           PatientLoginRepository patientLoginRepository) {
        this.patientRepository = patientRepository;
        this.patientLoginRepository = patientLoginRepository;
    }
    
    /**
     * Create patient medical record using phone number
     * Will lookup patient_id from patient_login table
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> createPatient(@RequestBody Patient patient,
                                          @RequestParam String phone) {
        
        // Find patient_login by phone to get patient_id
        Optional<PatientLogin> login = patientLoginRepository.findByPhone(phone);
        if (login.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Patient not found. Please login first."));
        }
        
        String patientId = login.get().getPatientId();
        
        // Check if medical record already exists
        if (patientRepository.existsByPatientId(patientId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Medical record already exists for this patient"));
        }
        
        // Create medical record
        patient.setPatientId(patientId);
        patient.setCreatedAt(LocalDateTime.now());
        
        Patient saved = patientRepository.save(patient);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of(
                "message", "Medical record created successfully",
                "patientId", saved.getPatientId(),
                "patient", saved
            ));
    }
    
    /**
     * Get patient by patient_id (CVD0001, etc.)
     */
    @GetMapping("/{patientId}")
    public ResponseEntity<?> getPatient(@PathVariable String patientId) {
        Optional<Patient> patient = patientRepository.findByPatientId(patientId);
        
        if (patient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Medical record not found"));
        }
        
        return ResponseEntity.ok(patient.get());
    }
    
    /**
     * Get patient by phone number
     */
    @GetMapping("/phone/{phone}")
    public ResponseEntity<?> getPatientByPhone(@PathVariable String phone) {
        // First find patient_login
        Optional<PatientLogin> login = patientLoginRepository.findByPhone(phone);
        if (login.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Patient not found"));
        }
        
        // Then find medical record
        Optional<Patient> patient = patientRepository.findByPatientId(login.get().getPatientId());
        
        if (patient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "error", "Medical record not found",
                    "patientId", login.get().getPatientId(),
                    "hasLogin", true,
                    "hasMedicalRecord", false
                ));
        }
        
        return ResponseEntity.ok(patient.get());
    }
    
    /**
     * Update patient medical record
     */
    @PutMapping("/{patientId}")
    @Transactional
    public ResponseEntity<?> updatePatient(@PathVariable String patientId,
                                          @RequestBody Patient updatedPatient) {
        Optional<Patient> existing = patientRepository.findByPatientId(patientId);
        
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Medical record not found"));
        }
        
        Patient patient = existing.get();
        
        // Update medical fields
        if (updatedPatient.getAge() != null) patient.setAge(updatedPatient.getAge());
        if (updatedPatient.getSex() != null) patient.setSex(updatedPatient.getSex());
        if (updatedPatient.getCp() != null) patient.setCp(updatedPatient.getCp());
        if (updatedPatient.getTrestbps() != null) patient.setTrestbps(updatedPatient.getTrestbps());
        if (updatedPatient.getChol() != null) patient.setChol(updatedPatient.getChol());
        if (updatedPatient.getFbs() != null) patient.setFbs(updatedPatient.getFbs());
        if (updatedPatient.getRestecg() != null) patient.setRestecg(updatedPatient.getRestecg());
        if (updatedPatient.getThalach() != null) patient.setThalach(updatedPatient.getThalach());
        if (updatedPatient.getExang() != null) patient.setExang(updatedPatient.getExang());
        if (updatedPatient.getOldpeak() != null) patient.setOldpeak(updatedPatient.getOldpeak());
        if (updatedPatient.getSlope() != null) patient.setSlope(updatedPatient.getSlope());
        if (updatedPatient.getCa() != null) patient.setCa(updatedPatient.getCa());
        if (updatedPatient.getThal() != null) patient.setThal(updatedPatient.getThal());
        
        Patient saved = patientRepository.save(patient);
        
        return ResponseEntity.ok(Map.of(
            "message", "Medical record updated successfully",
            "patient", saved
        ));
    }
}