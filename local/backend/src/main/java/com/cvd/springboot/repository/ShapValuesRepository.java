package com.cvd.springboot.repository;

import com.cvd.springboot.entity.ShapValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShapValuesRepository extends JpaRepository<ShapValues, Long> {
    
    Optional<ShapValues> findByPredictionSlNo(Long predictionSlNo);
    
    Optional<ShapValues> findByPatientId(String patientId);
}