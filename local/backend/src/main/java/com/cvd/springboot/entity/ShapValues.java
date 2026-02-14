package com.cvd.springboot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "shap_values")
public class ShapValues {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sl_no")
    private Long slNo;
    
    @Column(name = "patient_id", nullable = false)
    private String patientId;
    
    @Column(name = "prediction_sl_no", nullable = false)
    private Long predictionSlNo;
    
    // Individual SHAP values
    @Column(name = "shap_age")
    private Double shapAge;
    
    @Column(name = "shap_sex")
    private Double shapSex;
    
    @Column(name = "shap_cp")
    private Double shapCp;
    
    @Column(name = "shap_trestbps")
    private Double shapTrestbps;
    
    @Column(name = "shap_chol")
    private Double shapChol;
    
    @Column(name = "shap_fbs")
    private Double shapFbs;
    
    @Column(name = "shap_restecg")
    private Double shapRestecg;
    
    @Column(name = "shap_thalach")
    private Double shapThalach;
    
    @Column(name = "shap_exang")
    private Double shapExang;
    
    @Column(name = "shap_oldpeak")
    private Double shapOldpeak;
    
    @Column(name = "shap_slope")
    private Double shapSlope;
    
    @Column(name = "shap_ca")
    private Double shapCa;
    
    @Column(name = "shap_thal")
    private Double shapThal;
    
    // Getters
    public Long getSlNo() { return slNo; }
    public String getPatientId() { return patientId; }
    public Long getPredictionSlNo() { return predictionSlNo; }
    public Double getShapAge() { return shapAge; }
    public Double getShapSex() { return shapSex; }
    public Double getShapCp() { return shapCp; }
    public Double getShapTrestbps() { return shapTrestbps; }
    public Double getShapChol() { return shapChol; }
    public Double getShapFbs() { return shapFbs; }
    public Double getShapRestecg() { return shapRestecg; }
    public Double getShapThalach() { return shapThalach; }
    public Double getShapExang() { return shapExang; }
    public Double getShapOldpeak() { return shapOldpeak; }
    public Double getShapSlope() { return shapSlope; }
    public Double getShapCa() { return shapCa; }
    public Double getShapThal() { return shapThal; }
    
    // Setters
    public void setSlNo(Long slNo) { this.slNo = slNo; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public void setPredictionSlNo(Long predictionSlNo) { this.predictionSlNo = predictionSlNo; }
    public void setShapAge(Double shapAge) { this.shapAge = shapAge; }
    public void setShapSex(Double shapSex) { this.shapSex = shapSex; }
    public void setShapCp(Double shapCp) { this.shapCp = shapCp; }
    public void setShapTrestbps(Double shapTrestbps) { this.shapTrestbps = shapTrestbps; }
    public void setShapChol(Double shapChol) { this.shapChol = shapChol; }
    public void setShapFbs(Double shapFbs) { this.shapFbs = shapFbs; }
    public void setShapRestecg(Double shapRestecg) { this.shapRestecg = shapRestecg; }
    public void setShapThalach(Double shapThalach) { this.shapThalach = shapThalach; }
    public void setShapExang(Double shapExang) { this.shapExang = shapExang; }
    public void setShapOldpeak(Double shapOldpeak) { this.shapOldpeak = shapOldpeak; }
    public void setShapSlope(Double shapSlope) { this.shapSlope = shapSlope; }
    public void setShapCa(Double shapCa) { this.shapCa = shapCa; }
    public void setShapThal(Double shapThal) { this.shapThal = shapThal; }
}