package com.cvd.springboot.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "predictions")
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double riskScore;
    private String riskLevel;
    private LocalDateTime predictedAt;

    // 🔥 REQUIRED RELATIONSHIP
    @ManyToOne
    @JoinColumn(name = "patient_id")
    @JsonBackReference
    private Patient patient;

    // ===== GETTERS =====
    public Long getId() { return id; }
    public Double getRiskScore() { return riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public LocalDateTime getPredictedAt() { return predictedAt; }
    public Patient getPatient() { return patient; }

    // ===== SETTERS =====
    public void setId(Long id) { this.id = id; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public void setPredictedAt(LocalDateTime predictedAt) { this.predictedAt = predictedAt; }
    public void setPatient(Patient patient) { this.patient = patient; }
}
