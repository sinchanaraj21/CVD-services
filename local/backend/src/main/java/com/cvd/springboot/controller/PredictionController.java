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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.fasterxml.jackson.databind.ObjectMapper;


@CrossOrigin(origins = "http://localhost:3000")

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private final ObjectMapper objectMapper;
    private final PatientRepository patientRepository;
    private final PredictionRepository predictionRepository;
    private final MlClientService mlClientService;

    public PredictionController(
            PatientRepository patientRepository,
            PredictionRepository predictionRepository,
            MlClientService mlClientService,
            ObjectMapper objectMapper
    ) {
        this.patientRepository = patientRepository;
        this.predictionRepository = predictionRepository;
        this.mlClientService = mlClientService;
        this.objectMapper = objectMapper;
    }


    @PostMapping("/{patientId}")
    public ResponseEntity<Prediction> createPrediction(@PathVariable Long patientId) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

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

        MlPredictionResponse mlResponse =
                mlClientService.getPrediction(mlRequest);

        Prediction prediction = new Prediction();

        prediction.setPredictedAt(LocalDateTime.now());
        prediction.setRiskScore(mlResponse.getRiskScore());
        prediction.setRiskLevel(mlResponse.getRiskLevel());
        try {

        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize SHAP values", e);
        }

        Prediction saved = predictionRepository.save(prediction);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
