package com.cvd.springboot.service;

import com.cvd.springboot.dto.MlPredictionRequest;
import com.cvd.springboot.dto.MlPredictionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class MlClientService {

    private final WebClient webClient;

    @Value("${ml.api.key}")
    private String mlApiKey;

    public MlClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    public MlPredictionResponse getPrediction(MlPredictionRequest request) {

        return webClient.post()
                .uri("/predict")
                .header("X-API-KEY", mlApiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MlPredictionResponse.class)
                .block(); // OK for now (sync)
    }
}
