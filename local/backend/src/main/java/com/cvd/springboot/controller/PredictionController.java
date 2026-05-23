package com.cvd.springboot.controller;

import com.cvd.springboot.dto.MlPredictionRequest;
import com.cvd.springboot.dto.MlPredictionResponse;
import com.cvd.springboot.entity.Patient;
import com.cvd.springboot.entity.Prediction;
import com.cvd.springboot.repository.PatientRepository;
import com.cvd.springboot.repository.PredictionRepository;
import com.cvd.springboot.service.MlClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private final PatientRepository    patientRepository;
    private final PredictionRepository predictionRepository;
    private final MlClientService      mlClientService;

    public PredictionController(PatientRepository patientRepository,
                                PredictionRepository predictionRepository,
                                MlClientService mlClientService) {
        this.patientRepository    = patientRepository;
        this.predictionRepository = predictionRepository;
        this.mlClientService      = mlClientService;
    }

    // ════════════════════════════════════════════════
    // POST /api/predictions/{patientId}
    // Run the ML model and save the prediction.
    // Doctor-review creation removed — doctor role no longer exists.
    // ════════════════════════════════════════════════
    @PostMapping("/{patientId}")
    @Transactional
    public ResponseEntity<?> createPrediction(@PathVariable String patientId) {

        Patient patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new RuntimeException(
                        "Patient medical record not found with id: " + patientId));

        MlPredictionRequest  mlRequest  = buildMlRequest(patient);
        MlPredictionResponse mlResponse = mlClientService.getPrediction(mlRequest);

        Map<String, Double> shap = mlResponse.getShapValues();
        shap.replaceAll((k, v) -> Math.round(v * 1000.0) / 1000.0);

        Prediction prediction = new Prediction();
        prediction.setPatientId(patient.getPatientId());
        prediction.setPredictedAt(LocalDateTime.now());
        prediction.setRiskScore(mlResponse.getRiskScore());
        prediction.setRiskLevel(mlResponse.getRiskLevel());
        prediction.setShapAge(shap.get("age"));
        prediction.setShapSex(shap.get("sex"));
        prediction.setShapCp(shap.get("cp"));
        prediction.setShapTrestbps(shap.get("trestbps"));
        prediction.setShapChol(shap.get("chol"));
        prediction.setShapFbs(shap.get("fbs"));
        prediction.setShapRestecg(shap.get("restecg"));
        prediction.setShapThalach(shap.get("thalach"));
        prediction.setShapExang(shap.get("exang"));
        prediction.setShapOldpeak(shap.get("oldpeak"));
        prediction.setShapSlope(shap.get("slope"));
        prediction.setShapCa(shap.get("ca"));
        prediction.setShapThal(shap.get("thal"));

        Prediction saved = predictionRepository.save(prediction);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ════════════════════════════════════════════════
    // GET /api/predictions/patient/{patientId}
    // ════════════════════════════════════════════════
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientPredictions(@PathVariable String patientId) {
        return ResponseEntity.ok(
                predictionRepository.findByPatientIdOrderByPredictedAtDesc(patientId));
    }

    // ════════════════════════════════════════════════
    // DELETE /api/predictions/admin/{slNo}
    // ════════════════════════════════════════════════
    @DeleteMapping("/admin/{slNo}")
    @Transactional
    public ResponseEntity<?> deletePrediction(@PathVariable Long slNo) {
        if (!predictionRepository.existsById(slNo)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Prediction not found: " + slNo));
        }
        predictionRepository.deleteById(slNo);
        return ResponseEntity.ok(Map.of("message", "Prediction " + slNo + " deleted successfully"));
    }

    // ─── helper ──────────────────────────────────────
    private MlPredictionRequest buildMlRequest(Patient patient) {
        MlPredictionRequest req = new MlPredictionRequest();
        req.setAge(patient.getAge());
        req.setSex(patient.getSex());
        req.setCp(patient.getCp());
        req.setTrestbps(patient.getTrestbps());
        req.setChol(patient.getChol());
        req.setFbs(patient.getFbs());
        req.setRestecg(patient.getRestecg());
        req.setThalach(patient.getThalach());
        req.setExang(patient.getExang());
        req.setOldpeak(patient.getOldpeak());
        req.setSlope(patient.getSlope());
        req.setCa(patient.getCa());
        req.setThal(patient.getThal());
        return req;
    }
}
