package com.cvd.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;


public class MlPredictionResponse {

    @JsonProperty("risk_probability")
    private Double riskScore;

    @JsonProperty("risk_category")
    private String riskLevel;
    @JsonProperty("shap_values")
    private Map<String, Double> shapValues;


    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
    public Map<String, Double> getShapValues() {
        return shapValues;
    }

    public void setShapValues(Map<String, Double> shapValues) {
        this.shapValues = shapValues;
    }
}


