package com.cvd.springboot.service;

import com.cvd.springboot.dto.MlPredictionRequest;
import com.cvd.springboot.dto.MlPredictionResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class MlClientService {

    private final WebClient webClient;

    public MlClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    public MlPredictionResponse getPrediction(MlPredictionRequest request) {

        return webClient.post()
                .uri("/predict")
                .header("X-API-KEY", "cvd_ml_internal_key_2024")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MlPredictionResponse.class)
                .block(); // OK for now (sync)
    }
}
