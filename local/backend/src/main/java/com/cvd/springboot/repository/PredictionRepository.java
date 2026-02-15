package com.cvd.springboot.repository;

import com.cvd.springboot.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    List<Prediction> findByPatientIdOrderByPredictedAtDesc(String patientId);

    void deleteByPatientId(String patientId);
}
