package com.cvd.springboot.controller;

import com.cvd.springboot.dto.MlPredictionRequest;
import com.cvd.springboot.dto.MlPredictionResponse;
import com.cvd.springboot.entity.Patient;
import com.cvd.springboot.entity.Prediction;
import com.cvd.springboot.entity.ShapValues;
import com.cvd.springboot.repository.PatientRepository;
import com.cvd.springboot.repository.PredictionRepository;
import com.cvd.springboot.repository.ShapValuesRepository;
import com.cvd.springboot.service.MlClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    
    private final ObjectMapper objectMapper;
    private final PatientRepository patientRepository;
    private final PredictionRepository predictionRepository;
    private final MlClientService mlClientService;
    private final ShapValuesRepository shapValuesRepository;
    
    public PredictionController(
            PatientRepository patientRepository,
            PredictionRepository predictionRepository,
            MlClientService mlClientService,
            ObjectMapper objectMapper,
            ShapValuesRepository shapValuesRepository
    ) {
        this.patientRepository = patientRepository;
        this.predictionRepository = predictionRepository;
        this.mlClientService = mlClientService;
        this.objectMapper = objectMapper;
        this.shapValuesRepository = shapValuesRepository;
    }
    
    @PostMapping("/{patientId}")
    @Transactional
    public ResponseEntity<?> createPrediction(@PathVariable String patientId) {
        
        // 1. Find patient by patientId (CVD0001, etc.)
        Patient patient = patientRepository.findByPatientId(patientId)
            .orElseThrow(() -> new RuntimeException("Patient medical record not found with id: " + patientId));
        
        // 2. Build ML request
        MlPredictionRequest mlRequest = buildMlRequest(patient);
        
        // 3. Get prediction from ML service
        MlPredictionResponse mlResponse = mlClientService.getPrediction(mlRequest);
        
        // 4. Create and save prediction
        Prediction prediction = new Prediction();
        prediction.setPatientId(patient.getPatientId());
        prediction.setPredictedAt(LocalDateTime.now());
        prediction.setRiskScore(mlResponse.getRiskScore());
        prediction.setRiskLevel(mlResponse.getRiskLevel());
        
        Prediction saved = predictionRepository.save(prediction);
        
        // 5. Save SHAP values
        // Step 5: Save SHAP values
try {
    Map<String, Double> rounded = mlResponse.getShapValues().entrySet()
        .stream()
        .collect(java.util.stream.Collectors.toMap(
            Map.Entry::getKey,
            e -> Math.round(e.getValue() * 100.0) / 100.0
        ));
    
    ShapValues shap = new ShapValues();
    shap.setPatientId(patient.getPatientId());
    shap.setPredictionSlNo(saved.getSlNo());
    
    // Set individual SHAP values
    shap.setShapAge(rounded.get("age"));
    shap.setShapSex(rounded.get("sex"));
    shap.setShapCp(rounded.get("cp"));
    shap.setShapTrestbps(rounded.get("trestbps"));
    shap.setShapChol(rounded.get("chol"));
    shap.setShapFbs(rounded.get("fbs"));
    shap.setShapRestecg(rounded.get("restecg"));
    shap.setShapThalach(rounded.get("thalach"));
    shap.setShapExang(rounded.get("exang"));
    shap.setShapOldpeak(rounded.get("oldpeak"));
    shap.setShapSlope(rounded.get("slope"));
    shap.setShapCa(rounded.get("ca"));
    shap.setShapThal(rounded.get("thal"));
    
    shapValuesRepository.save(shap);
    
} catch (Exception e) {
    throw new RuntimeException("Failed to save SHAP values for prediction: " + saved.getSlNo(), e);
}
        
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientPredictions(@PathVariable String patientId) {
        return ResponseEntity.ok(
            predictionRepository.findByPatientIdOrderByPredictedAtDesc(patientId)
        );
    }
    
    // Helper method to build ML request
    private MlPredictionRequest buildMlRequest(Patient patient) {
        MlPredictionRequest mlRequest = new MlPredictionRequest();
        mlRequest.setAge(patient.getAge());
        mlRequest.setSex(patient.getSex());
        mlRequest.setCp(patient.getCp());
        mlRequest.setTrestbps(patient.getTrestbps());
        mlRequest.setChol(patient.getChol());
        mlRequest.setFbs(patient.getFbs());
        mlRequest.setRestecg(patient.getRestecg());
        mlRequest.setThalach(patient.getThalach());
        mlRequest.setExang(patient.getExang());
        mlRequest.setOldpeak(patient.getOldpeak());
        mlRequest.setSlope(patient.getSlope());
        mlRequest.setCa(patient.getCa());
        mlRequest.setThal(patient.getThal());
        return mlRequest;
    }
}