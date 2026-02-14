package com.cvd.springboot.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "predictions")
public class Prediction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sl_no")
    private Long slNo;
    
    @Column(name = "patient_id", nullable = false)
    private String patientId;
    
    private Double riskScore;
    private String riskLevel;
    private LocalDateTime predictedAt;
    
    // Relationship for convenience
    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "patient_id", 
                insertable = false, updatable = false)
    @JsonBackReference
    private Patient patient;
    
    // ===== GETTERS =====
    public Long getSlNo() { return slNo; }
    public String getPatientId() { return patientId; }
    public Double getRiskScore() { return riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public LocalDateTime getPredictedAt() { return predictedAt; }
    public Patient getPatient() { return patient; }
    
    // ===== SETTERS =====
    public void setSlNo(Long slNo) { this.slNo = slNo; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public void setPredictedAt(LocalDateTime predictedAt) { this.predictedAt = predictedAt; }
    public void setPatient(Patient patient) { this.patient = patient; }
}