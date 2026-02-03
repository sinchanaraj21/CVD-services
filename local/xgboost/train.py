"""
CVD XGBoost Model Training Script
Trains a cardiovascular disease prediction model using XGBoost
Includes proper validation and model persistence
"""

import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, roc_auc_score
import xgboost as xgb
import pickle
import json
import shap
import matplotlib.pyplot as plt

def load_and_prepare_data(filepath='data/heart.csv'):
    """Load and prepare the heart disease dataset"""
    print("Loading dataset...")
    df = pd.read_csv(filepath)
    
    print(f"Dataset shape: {df.shape}")
    print(f"\nFeature columns: {list(df.columns[:-1])}")
    print(f"Target distribution:\n{df['target'].value_counts()}")
    
    # Separate features and target
    X = df.drop('target', axis=1)
    y = 1-df['target']
    
    return X, y

def train_model(X, y):
    """Train XGBoost classifier with proper validation"""
    print("\nSplitting data into train and test sets...")
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )
    
    print(f"Training set size: {len(X_train)}")
    print(f"Test set size: {len(X_test)}")
    
    print("\nTraining XGBoost model...")
    model = xgb.XGBClassifier(
        n_estimators=100,
        max_depth=4,
        learning_rate=0.1,
        subsample=0.8,
        colsample_bytree=0.8,
        random_state=42,
        eval_metric='logloss'
    )
    
    model.fit(X_train, y_train)
    
    print("\nEvaluating model...")
    # Training metrics
    y_train_pred = model.predict(X_train)
    y_train_proba = model.predict_proba(X_train)[:, 1]
    
    # Test metrics
    y_test_pred = model.predict(X_test)
    y_test_proba = model.predict_proba(X_test)[:, 1]
    
    metrics = {
        'train': {
            'accuracy': accuracy_score(y_train, y_train_pred),
            'precision': precision_score(y_train, y_train_pred),
            'recall': recall_score(y_train, y_train_pred),
            'f1': f1_score(y_train, y_train_pred),
            'auc': roc_auc_score(y_train, y_train_proba)
        },
        'test': {
            'accuracy': accuracy_score(y_test, y_test_pred),
            'precision': precision_score(y_test, y_test_pred),
            'recall': recall_score(y_test, y_test_pred),
            'f1': f1_score(y_test, y_test_pred),
            'auc': roc_auc_score(y_test, y_test_proba)
        }
    }
    
    print("\n" + "="*50)
    print("MODEL PERFORMANCE METRICS")
    print("="*50)
    
    for dataset in ['train', 'test']:
        print(f"\n{dataset.upper()} SET:")
        for metric, value in metrics[dataset].items():
            print(f"  {metric.capitalize()}: {value:.4f}")
    
    # Feature importance
    feature_importance = pd.DataFrame({
        'feature': X.columns,
        'importance': model.feature_importances_
    }).sort_values('importance', ascending=False)
    
    print("\n" + "="*50)
    print("TOP 10 FEATURE IMPORTANCES")
    print("="*50)
    print(feature_importance.head(10).to_string(index=False))
        # -----------------------------
    # STEP 3: SHAP Explainability
    # -----------------------------
    print("\nGenerating SHAP explanations...")

    # TreeExplainer for XGBoost
    explainer = shap.TreeExplainer(model)

    # Background data (for stable SHAP values)
    X_background = X_train.sample(
        n=min(100, len(X_train)),
        random_state=42
    )

    shap_values = explainer.shap_values(X_background)

    # -----------------------------
    # Global SHAP Feature Importance
    # -----------------------------
    print("Saving global SHAP feature importance plot...")
    shap.summary_plot(
        shap_values,
        X_background,
        plot_type="bar",
        show=False
    )
    plt.tight_layout()
    plt.savefig("shap_global_importance.png")
    plt.close()

    # -----------------------------
    # Individual Prediction SHAP
    # -----------------------------
    sample_data = X_test.iloc[[0]]
    shap_sample = explainer.shap_values(sample_data)

    print("Saving individual SHAP explanation plot...")
    shap.waterfall_plot(
        shap.Explanation(
            values=shap_sample[0],
            base_values=explainer.expected_value,
            data=sample_data.iloc[0],
            feature_names=sample_data.columns
        ),
        show=False
    )
    plt.tight_layout()
    plt.savefig("shap_individual_explanation.png")
    plt.close()

    print("SHAP explanations saved successfully.")

    
    return model, metrics

def save_model(model, filename='model.pkl'):
    """Save trained model to disk"""
    print(f"\nSaving model to {filename}...")
    with open(filename, 'wb') as f:
        pickle.dump(model, f)
    print("Model saved successfully!")

def main():
    print("="*50)
    print("CVD XGBoost Model Training")
    print("="*50)
    
    # Load data
    X, y = load_and_prepare_data()
    
    # Train model
    model, metrics = train_model(X, y)
    
    # Save model
    save_model(model)
    
    # Save metrics
    with open('training_metrics.json', 'w') as f:
        json.dump(metrics, f, indent=2)
    print("Training metrics saved to training_metrics.json")
    
    print("\n" + "="*50)
    print("Training completed successfully!")
    print("="*50)

if __name__ == "__main__":
    main()