package com.cvd.springboot.dto;

public class MlPredictionRequest {

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

    // Getters & Setters
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Integer getSex() { return sex; }
    public void setSex(Integer sex) { this.sex = sex; }

    public Integer getCp() { return cp; }
    public void setCp(Integer cp) { this.cp = cp; }

    public Integer getTrestbps() { return trestbps; }
    public void setTrestbps(Integer trestbps) { this.trestbps = trestbps; }

    public Integer getChol() { return chol; }
    public void setChol(Integer chol) { this.chol = chol; }

    public Integer getFbs() { return fbs; }
    public void setFbs(Integer fbs) { this.fbs = fbs; }

    public Integer getRestecg() { return restecg; }
    public void setRestecg(Integer restecg) { this.restecg = restecg; }

    public Integer getThalach() { return thalach; }
    public void setThalach(Integer thalach) { this.thalach = thalach; }

    public Integer getExang() { return exang; }
    public void setExang(Integer exang) { this.exang = exang; }

    public Integer getOldpeak() { return oldpeak; }
    public void setOldpeak(Integer oldpeak) { this.oldpeak = oldpeak; }

    public Integer getSlope() { return slope; }
    public void setSlope(Integer slope) { this.slope = slope; }

    public Integer getCa() { return ca; }
    public void setCa(Integer ca) { this.ca = ca; }

    public Integer getThal() { return thal; }
    public void setThal(Integer thal) { this.thal = thal; }
}
