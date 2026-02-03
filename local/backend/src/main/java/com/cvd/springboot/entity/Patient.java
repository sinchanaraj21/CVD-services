package com.cvd.springboot.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;

    private Integer age;
    private Integer sex;
    private Integer cp;
    private Integer trestbps;
    private Integer chol;
    private Integer fbs;
    private Integer restecg;
    private Integer thalach;
    private Integer exang;
    private Integer oldpeak;
    private Integer slope;
    private Integer ca;
    private Integer thal;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Prediction> predictions;

    // ===== GETTERS =====

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Integer getAge() { return age; }
    public Integer getSex() { return sex; }
    public Integer getCp() { return cp; }
    public Integer getTrestbps() { return trestbps; }
    public Integer getChol() { return chol; }
    public Integer getFbs() { return fbs; }
    public Integer getRestecg() { return restecg; }
    public Integer getThalach() { return thalach; }
    public Integer getExang() { return exang; }
    public Integer getOldpeak() { return oldpeak; }
    public Integer getSlope() { return slope; }
    public Integer getCa() { return ca; }
    public Integer getThal() { return thal; }
    public List<Prediction> getPredictions() { return predictions; }

    // ===== SETTERS =====

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setAge(Integer age) { this.age = age; }
    public void setSex(Integer sex) { this.sex = sex; }
    public void setCp(Integer cp) { this.cp = cp; }
    public void setTrestbps(Integer trestbps) { this.trestbps = trestbps; }
    public void setChol(Integer chol) { this.chol = chol; }
    public void setFbs(Integer fbs) { this.fbs = fbs; }
    public void setRestecg(Integer restecg) { this.restecg = restecg; }
    public void setThalach(Integer thalach) { this.thalach = thalach; }
    public void setExang(Integer exang) { this.exang = exang; }
    public void setOldpeak(Integer oldpeak) { this.oldpeak = oldpeak; }
    public void setSlope(Integer slope) { this.slope = slope; }
    public void setCa(Integer ca) { this.ca = ca; }
    public void setThal(Integer thal) { this.thal = thal; }
    public void setPredictions(List<Prediction> predictions) { this.predictions = predictions; }
}
