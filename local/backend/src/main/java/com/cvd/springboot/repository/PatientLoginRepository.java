package com.cvd.springboot.repository;

import com.cvd.springboot.entity.PatientLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PatientLoginRepository extends JpaRepository<PatientLogin, Long> {
    
    @Query("SELECT MAX(p.slNo) FROM PatientLogin p")
    Long findMaxSlNo();
    
    Optional<PatientLogin> findByPhone(String phone);
    
    Optional<PatientLogin> findByPatientId(String patientId);
    
    boolean existsByPhone(String phone);
}