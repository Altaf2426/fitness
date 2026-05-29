package com.fitness.activityservice.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@AllArgsConstructor
@Slf4j
public class UserValidationService {
    private final WebClient userServiceWebClient;

    public Boolean validateUser(String userId){
        try{
            return userServiceWebClient.get()
                .uri("/api/user/{userId}/validate" , userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        }catch (WebClientResponseException e){
            e.printStackTrace();
        }
        return false;
    }
}
