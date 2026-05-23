"""
CVD FastAPI Prediction Service
Stateless ML inference service with SHAP explainability
Protected by API key authentication
"""

from fastapi import FastAPI, HTTPException, Security, Header
from fastapi.security import APIKeyHeader
from pydantic import BaseModel, Field
import pickle
import numpy as np
import pandas as pd
import shap
from typing import List, Dict, Optional, Any
import uvicorn
import os

# Initialize FastAPI app
app = FastAPI(
    title="CVD Prediction Service",
    description="Explainable cardiovascular disease risk prediction using XGBoost and SHAP",
    version="1.0.0"
)

# API Key — read from environment variable. Fallback is for local dev only.
API_KEY = os.environ.get("CVD_ML_API_KEY", "cvd_ml_internal_key_2024")
api_key_header = APIKeyHeader(name="X-API-KEY", auto_error=True)

# Global variables for model and explainer
model = None
explainer = None
feature_names = None

class PatientFeatures(BaseModel):
    """Patient clinical features for prediction"""
    age: int = Field(..., ge=1, le=120, description="Age in years")
    sex: int = Field(..., ge=0, le=1, description="Sex (1=male, 0=female)")
    cp: int = Field(..., ge=0, le=3, description="Chest pain type (0-3)")
    trestbps: int = Field(..., ge=80, le=200, description="Resting blood pressure (mm Hg)")
    chol: int = Field(..., ge=100, le=600, description="Serum cholesterol (mg/dl)")
    fbs: int = Field(..., ge=0, le=1, description="Fasting blood sugar > 120 mg/dl (1=true, 0=false)")
    restecg: int = Field(..., ge=0, le=2, description="Resting ECG results (0-2)")
    thalach: int = Field(..., ge=60, le=220, description="Maximum heart rate achieved")
    exang: int = Field(..., ge=0, le=1, description="Exercise induced angina (1=yes, 0=no)")
    oldpeak: float = Field(..., ge=0, le=10, description="ST depression induced by exercise")
    slope: int = Field(..., ge=0, le=2, description="Slope of peak exercise ST segment (0-2)")
    ca: int = Field(..., ge=0, le=4, description="Number of major vessels colored by fluoroscopy")
    thal: int = Field(..., ge=0, le=3, description="Thalassemia (0=normal, 1=fixed defect, 2=reversible defect, 3=unknown)")

class PredictionResponse(BaseModel):
    """Prediction response with risk assessment and explanations"""
    risk_probability: float
    risk_category: str
    risk_percentage: float
    shap_values: Dict[str, float]
    top_risk_factors: List[Dict[str, Any]]
    prediction: int

def verify_api_key(api_key: str = Security(api_key_header)):
    """Verify API key for authentication"""
    if api_key != API_KEY:
        raise HTTPException(
            status_code=403,
            detail="Invalid API key"
        )
    return api_key

def categorize_risk(probability: float) -> str:
    """Categorize risk based on probability"""
    if probability < 0.3:
        return "Low"
    elif probability < 0.7:
        return "Medium"
    else:
        return "High"

@app.on_event("startup")
async def load_model_and_explainer():
    """Load model and initialize SHAP explainer on startup"""
    global model, explainer, feature_names
    
    print("Loading XGBoost model...")
    try:
        with open('model.pkl', 'rb') as f:
            model = pickle.load(f)
        print("Model loaded successfully!")
        
        # Get feature names
        feature_names = ['age', 'sex', 'cp', 'trestbps', 'chol', 'fbs', 'restecg', 
                        'thalach', 'exang', 'oldpeak', 'slope', 'ca', 'thal']
        
        # Initialize SHAP explainer
        print("Initializing SHAP explainer...")
        # Load training data for SHAP background
        train_data = pd.read_csv('data/heart.csv')
        X_train = train_data.drop('target', axis=1)
        
        # Use a sample for SHAP background (faster computation)
        background = shap.sample(X_train, 100)
        explainer = shap.TreeExplainer(model, background)
        print("SHAP explainer initialized successfully!")
        
    except Exception as e:
        print(f"Error loading model or initializing explainer: {e}")
        raise

@app.get("/")
async def root():
    """Health check endpoint"""
    return {
        "service": "CVD Prediction Service",
        "status": "running",
        "model_loaded": model is not None,
        "explainer_ready": explainer is not None
    }

@app.get("/health")
async def health_check():
    """Detailed health check"""
    return {
        "status": "healthy",
        "model": "loaded" if model else "not loaded",
        "explainer": "ready" if explainer else "not ready"
    }

@app.post("/predict", response_model=PredictionResponse)
async def predict(
    patient: PatientFeatures,
    api_key: str = Security(api_key_header)
):
    """
    Predict cardiovascular disease risk with SHAP explanations
    
    Requires X-API-KEY header for authentication
    """
    verify_api_key(api_key)
    
    if model is None or explainer is None:
        raise HTTPException(
            status_code=503,
            detail="Model or explainer not loaded"
        )
    
    try:
        # Prepare input data
        input_data = pd.DataFrame([patient.dict()])
        
        # Ensure correct feature order
        input_data = input_data[feature_names]
        
        # Make prediction
        disease_class_index = list(model.classes_).index(1)
        probability = model.predict_proba(input_data)[0][disease_class_index]
        prediction = int(probability >= 0.5)
        risk_percentage = round(float(probability * 100), 2)  # Round to 2 decimals
        risk_category = categorize_risk(probability)
        
        # Get SHAP values
        shap_exp = explainer(input_data)

        if isinstance(shap_exp.values, list):
            shap_vals = shap_exp.values[1][0]
        else:
            shap_vals = shap_exp.values[0]

        
        # Create feature contribution dictionary - ROUNDED TO 3 DECIMALS
        shap_dict = {
            feature: round(float(value), 3)  # Round to 3 decimal places
            for feature, value in zip(feature_names, shap_vals)
        }
        
        # Get top risk factors (features with highest absolute SHAP values)
        feature_contributions = [
            {
                "feature": feature,
                "shap_value": round(float(value), 3),  # Round to 3 decimals
                "actual_value": float(input_data[feature].values[0]),
                "contribution": "increases risk" if value > 0 else "decreases risk"
            }
            for feature, value in zip(feature_names, shap_vals)
        ]
        
        # Sort by absolute SHAP value
        top_risk_factors = sorted(
            feature_contributions,
            key=lambda x: abs(x['shap_value']),
            reverse=True
        )[:5]  # Top 5 factors
        
        return PredictionResponse(
            risk_probability=round(float(probability), 3),  # Round to 3 decimals
            risk_category=risk_category,
            risk_percentage=risk_percentage,
            shap_values=shap_dict,
            top_risk_factors=top_risk_factors,
            prediction=prediction
        )
        
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Prediction error: {str(e)}"
        )

@app.post("/batch_predict")
async def batch_predict(
    patients: List[PatientFeatures],
    api_key: str = Security(api_key_header)
):
    """
    Batch prediction endpoint for multiple patients
    
    Requires X-API-KEY header for authentication
    """
    verify_api_key(api_key)
    
    results = []
    for patient in patients:
        result = await predict(patient, api_key)
        results.append(result)
    
    return {"predictions": results}

if __name__ == "__main__":
    print("Starting CVD Prediction Service...")
    print("Service will be available at http://localhost:5001")
    
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=5001,
        log_level="info"
    )
