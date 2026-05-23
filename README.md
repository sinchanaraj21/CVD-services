# CardioPredict — Cardiovascular Disease Risk Prediction

> An end-to-end clinical-grade web application that predicts cardiovascular disease risk using XGBoost and explains every prediction with SHAP values.

---

## Overview

CardioPredict lets patients enter their clinical measurements, runs them through a trained XGBoost model, and returns a risk score with an explanation of which factors drove the result — powered by SHAP. Admins can manage patients and view history. Researchers get a public dashboard with population-level analytics.

### Architecture

```
React (port 3000)
    │  REST / JWT
    ▼
Spring Boot (port 8080)
    │  REST + API key
    ▼
FastAPI + XGBoost + SHAP (port 5001)
    │
PostgreSQL (port 5432)
```

---

## Tech Stack

| Layer      | Technology                              |
|------------|------------------------------------------|
| Frontend   | React 19, SCSS, Axios                   |
| Backend    | Spring Boot 3.2, Spring Security, JWT   |
| ML Service | FastAPI, XGBoost 2.0, SHAP 0.44         |
| Database   | PostgreSQL 15                           |
| Container  | Docker, Docker Compose                  |

---

## Features

- **Patient portal** — mobile OTP login, clinical data entry, risk assessment, prediction history
- **Admin dashboard** — patient management, search by ID or phone, delete records
- **Researcher dashboard** — population statistics, risk distribution, SHAP feature importance, research trends (public, no login required)
- **Explainable AI** — every prediction includes per-feature SHAP values and a ranked list of top risk drivers
- **JWT authentication** — role-based access (Patient / Admin)
- **Dark / light theme**

---

## Model Performance

Trained on the [UCI Heart Disease dataset](https://archive.ics.uci.edu/dataset/45/heart+disease) (303 records).

| Metric    | Train  | Test   |
|-----------|--------|--------|
| Accuracy  | 99.2 % | 83.6 % |
| Precision | 99.1 % | 84.6 % |
| Recall    | 99.1 % | 78.6 % |
| F1        | 99.1 % | 81.5 % |
| AUC-ROC   | 99.99% | 89.4 % |

> ⚠️ This model is for educational and research purposes only. It is **not** a substitute for professional medical diagnosis.

---

## Quickstart (Docker)

### Prerequisites
- Docker Desktop (or Docker Engine + Compose)

### 1. Clone the repo

```bash
git clone https://github.com/<your-username>/cvd.git
cd cvd
```

### 2. Set environment variables

```bash
cp .env.example .env
# Edit .env — set CVD_ML_API_KEY and CVD_JWT_SECRET
```

### 3. Start everything

```bash
docker compose up --build
```

| Service    | URL                        |
|------------|----------------------------|
| Frontend   | http://localhost:3000      |
| Backend    | http://localhost:8080      |
| ML Service | http://localhost:5001      |
| ML Docs    | http://localhost:5001/docs |

---

## Local Development (without Docker)

### PostgreSQL

```bash
# macOS (Homebrew)
brew services start postgresql@15
psql -U postgres -c "CREATE DATABASE cvd;"
```

### ML Service

```bash
cd local/xgboost
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt

# (Re)train the model — only needed once
python train.py

# Start the FastAPI server
python app.py
# → http://localhost:5001
```

### Backend

```bash
cd local/backend
export CVD_ML_API_KEY=cvd_ml_internal_key_2024
export CVD_JWT_SECRET=cvd_secret_key_for_jwt_token_generation_123456
export DB_USERNAME=postgres
export DB_PASSWORD=         # blank if no password

./mvnw spring-boot:run
# → http://localhost:8080
```

### Frontend

```bash
cd local/frontend
npm install
npm start
# → http://localhost:3000
```

---

## Environment Variables

| Variable         | Required | Description                                  |
|------------------|----------|----------------------------------------------|
| `CVD_ML_API_KEY` | Yes      | Shared secret between backend and ML service |
| `CVD_JWT_SECRET` | Yes      | At least 32 characters for JWT signing       |
| `DB_USERNAME`    | No       | PostgreSQL username (default: `postgres`)    |
| `DB_PASSWORD`    | No       | PostgreSQL password (default: empty)         |

Copy `.env.example` to `.env` and fill in real values. **Never commit `.env`.**

---

## Project Structure

```
cvd/
├── docker-compose.yml          # Orchestrates all four services
├── .env.example                # Environment variable template
├── scripts/
│   └── drop_doctor_tables.sql  # One-time migration for pre-v1.1 schemas
└── local/
    ├── frontend/               # React app (Create React App)
    │   └── src/
    │       ├── components/     # All page/feature components
    │       ├── context/        # AuthContext (JWT state)
    │       ├── hooks/          # useTheme
    │       ├── services/       # api.js — all fetch calls
    │       └── styles/         # Global SCSS tokens
    ├── backend/                # Spring Boot REST API
    │   └── src/main/java/com/cvd/springboot/
    │       ├── controller/     # REST endpoints
    │       ├── entity/         # JPA entities
    │       ├── repository/     # Spring Data repositories
    │       ├── security/       # JWT filter + config
    │       └── service/        # ML client, OTP, admin seeder
    └── xgboost/                # FastAPI ML microservice
        ├── app.py              # Prediction endpoints
        ├── train.py            # Model training script
        ├── requirements.txt
        └── data/heart.csv      # Training dataset
```

---

## API Reference

### Auth (`/api/auth`)

| Method | Endpoint              | Auth | Description                        |
|--------|-----------------------|------|------------------------------------|
| POST   | `/send-otp`           | None | Send OTP to patient mobile         |
| POST   | `/verify-otp`         | None | Verify OTP, register or login      |
| POST   | `/admin-login`        | None | Admin login with ID + password     |
| POST   | `/admin-register`     | None | Create admin account (secret key)  |

### Patients (`/api/patients`)

| Method | Endpoint              | Auth | Description                        |
|--------|-----------------------|------|------------------------------------|
| POST   | `/`                   | JWT  | Create medical record              |
| GET    | `/{patientId}`        | JWT  | Get patient record                 |
| GET    | `/phone/{phone}`      | None | Lookup by phone (pre-login)        |
| PUT    | `/{patientId}`        | JWT  | Update medical record              |
| GET    | `/admin/all`          | JWT  | List all patients (admin)          |
| GET    | `/admin/search?q=`    | JWT  | Search by ID or phone (admin)      |
| DELETE | `/admin/{patientId}`  | JWT  | Delete record + predictions        |

### Predictions (`/api/predictions`)

| Method | Endpoint                      | Auth | Description                    |
|--------|-------------------------------|------|--------------------------------|
| POST   | `/{patientId}`                | JWT  | Run ML prediction              |
| GET    | `/patient/{patientId}`        | JWT  | Get prediction history         |
| DELETE | `/admin/{slNo}`               | JWT  | Delete single prediction       |

### Researcher (`/api/researcher`)

| Method | Endpoint     | Auth | Description                           |
|--------|--------------|------|---------------------------------------|
| GET    | `/dashboard` | None | Population stats, SHAP, trends        |

### ML Service (`http://localhost:5001`)

| Method | Endpoint        | Auth (X-API-KEY) | Description               |
|--------|-----------------|------------------|---------------------------|
| GET    | `/`             | None             | Health + model status     |
| GET    | `/health`       | None             | Detailed health check     |
| POST   | `/predict`      | Yes              | Single patient prediction |
| POST   | `/batch_predict`| Yes              | Batch prediction          |

---

## Admin Setup

The first admin account must be created via `POST /api/auth/admin-register` with the secret key.

```json
{
  "secretKey": "CVD11166",
  "name": "Your Name",
  "adminId": "ADMIN01",
  "password": "yourpassword"
}
```

> Change `ADMIN_SECRET_KEY` in `AuthController.java` before deploying to production.

---

## Database Migration

If you have a pre-v1.1 database with the old Doctor tables, run:

```bash
psql -d cvd -U postgres -f scripts/drop_doctor_tables.sql
```

Fresh installs can skip this — Hibernate creates only the current schema.

---

## Author

**Sinchana Raj G**

---

## Disclaimer

CardioPredict is an academic project. Predictions are based on a small public dataset and are intended for educational and research use only. Always consult a qualified medical professional for health decisions.
