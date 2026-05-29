package com.fitnes.aiservice.service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class GeminiService {
    final private WebClient webClient;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public GeminiService(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.build();
    }

    public String getRecommendation(String detail){
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{

                        Map.of("parts", new Object[]{
                                        Map.of("text", detail)

                }
                        )
    }
        );
        String response = webClient.post()
                .uri(geminiApiUrl)
                .header("x-goog-api-key" , "AIzaSyAdtThFPe45m2ILdC_NZz_AI8leIqU2P8g")
                .header("Content-type" , "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return response;
    }
}
