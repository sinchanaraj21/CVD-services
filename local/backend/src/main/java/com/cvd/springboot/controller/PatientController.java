package com.cvd.springboot.controller;

import com.cvd.springboot.entity.Patient;
import com.cvd.springboot.entity.PatientLogin;
import com.cvd.springboot.repository.PatientLoginRepository;
import com.cvd.springboot.repository.PatientRepository;
import com.cvd.springboot.repository.PredictionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "http://localhost:3000")
public class PatientController {

    private final PatientRepository patientRepository;
    private final PatientLoginRepository patientLoginRepository;
    private final PredictionRepository predictionRepository;

    public PatientController(PatientRepository patientRepository,
                             PatientLoginRepository patientLoginRepository,
                             PredictionRepository predictionRepository) {
        this.patientRepository = patientRepository;
        this.patientLoginRepository = patientLoginRepository;
        this.predictionRepository = predictionRepository;
    }

    // ════════════════════════════════════════════════
    // PATIENT ROUTES
    // ════════════════════════════════════════════════

    @PostMapping
    @Transactional
    public ResponseEntity<?> createPatient(@RequestBody Patient patient,
                                           @RequestParam String phone) {
        Optional<PatientLogin> login = patientLoginRepository.findByPhone(phone);
        if (login.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Patient not found. Please login first."));
        }

        String patientId = login.get().getPatientId();

        if (patientRepository.existsByPatientId(patientId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Medical record already exists for this patient"));
        }

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

    @GetMapping("/{patientId}")
    public ResponseEntity<?> getPatient(@PathVariable String patientId) {
        Optional<Patient> patient = patientRepository.findByPatientId(patientId);
        if (patient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Medical record not found"));
        }
        return ResponseEntity.ok(patient.get());
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<?> getPatientByPhone(@PathVariable String phone) {
        Optional<PatientLogin> login = patientLoginRepository.findByPhone(phone);
        if (login.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Patient not found"));
        }

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
        if (updatedPatient.getAge() != null)      patient.setAge(updatedPatient.getAge());
        if (updatedPatient.getSex() != null)      patient.setSex(updatedPatient.getSex());
        if (updatedPatient.getCp() != null)       patient.setCp(updatedPatient.getCp());
        if (updatedPatient.getTrestbps() != null) patient.setTrestbps(updatedPatient.getTrestbps());
        if (updatedPatient.getChol() != null)     patient.setChol(updatedPatient.getChol());
        if (updatedPatient.getFbs() != null)      patient.setFbs(updatedPatient.getFbs());
        if (updatedPatient.getRestecg() != null)  patient.setRestecg(updatedPatient.getRestecg());
        if (updatedPatient.getThalach() != null)  patient.setThalach(updatedPatient.getThalach());
        if (updatedPatient.getExang() != null)    patient.setExang(updatedPatient.getExang());
        if (updatedPatient.getOldpeak() != null)  patient.setOldpeak(updatedPatient.getOldpeak());
        if (updatedPatient.getSlope() != null)    patient.setSlope(updatedPatient.getSlope());
        if (updatedPatient.getCa() != null)       patient.setCa(updatedPatient.getCa());
        if (updatedPatient.getThal() != null)     patient.setThal(updatedPatient.getThal());

        Patient saved = patientRepository.save(patient);
        return ResponseEntity.ok(Map.of("message", "Medical record updated successfully", "patient", saved));
    }

    // ════════════════════════════════════════════════
    // ADMIN ROUTES
    // ════════════════════════════════════════════════

    /**
     * GET /api/patients/admin/all
     * Returns all patient_login rows joined with their medical record (if exists)
     */
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllPatients() {
        List<PatientLogin> logins = patientLoginRepository.findAll();

        List<Map<String, Object>> result = logins.stream().map(login -> {
            Optional<Patient> medRecord = patientRepository.findByPatientId(login.getPatientId());
            Map<String, Object> entry = new java.util.LinkedHashMap<>();
            entry.put("patientId",    login.getPatientId());
            entry.put("name",         login.getName());
            entry.put("phone",        login.getPhone());
            entry.put("createdAt",    login.getCreatedAt());
            entry.put("lastLogin",    login.getLastLogin());
            entry.put("hasMedicalRecord", medRecord.isPresent());
            medRecord.ifPresent(p -> {
                entry.put("age",      p.getAge());
                entry.put("sex",      p.getSex());
                entry.put("cp",       p.getCp());
                entry.put("trestbps", p.getTrestbps());
                entry.put("chol",     p.getChol());
            });
            return entry;
        }).toList();

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/patients/admin/search?q=CVD0001  OR  ?q=9901352733
     * Searches by patientId prefix OR phone number
     */
    @GetMapping("/admin/search")
    public ResponseEntity<?> searchPatient(@RequestParam String q) {
        // Try phone first
        Optional<PatientLogin> byPhone = patientLoginRepository.findByPhone(q);
        if (byPhone.isPresent()) {
            return buildAdminPatientResponse(byPhone.get());
        }
        // Try patientId
        Optional<PatientLogin> byId = patientLoginRepository.findByPatientId(q.toUpperCase());
        if (byId.isPresent()) {
            return buildAdminPatientResponse(byId.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "No patient found for: " + q));
    }

    /**
     * DELETE /api/patients/admin/{patientId}
     * Deletes medical record + all predictions for this patient.
     * Does NOT delete the login entry (phone/name stays).
     */
    @DeleteMapping("/admin/{patientId}")
    @Transactional
    public ResponseEntity<?> deletePatientRecord(@PathVariable String patientId) {
        if (!patientRepository.existsByPatientId(patientId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Medical record not found for: " + patientId));
        }
        // Delete predictions first (FK constraint)
        predictionRepository.deleteByPatientId(patientId);
        // Delete medical record
        patientRepository.deleteByPatientId(patientId);

        return ResponseEntity.ok(Map.of(
                "message", "Medical record and all predictions deleted for " + patientId));
    }

    // ── private helper ───────────────────────────────
    private ResponseEntity<?> buildAdminPatientResponse(PatientLogin login) {
        Optional<Patient> med = patientRepository.findByPatientId(login.getPatientId());
        Map<String, Object> entry = new java.util.LinkedHashMap<>();
        entry.put("patientId",        login.getPatientId());
        entry.put("name",             login.getName());
        entry.put("phone",            login.getPhone());
        entry.put("createdAt",        login.getCreatedAt());
        entry.put("lastLogin",        login.getLastLogin());
        entry.put("hasMedicalRecord", med.isPresent());
        med.ifPresent(p -> {
            entry.put("age",      p.getAge());
            entry.put("sex",      p.getSex());
            entry.put("cp",       p.getCp());
            entry.put("trestbps", p.getTrestbps());
            entry.put("chol",     p.getChol());
        });
        return ResponseEntity.ok(entry);
    }
}
