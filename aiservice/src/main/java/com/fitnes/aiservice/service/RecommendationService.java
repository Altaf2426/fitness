package com.fitnes.aiservice.service;

import com.fitnes.aiservice.model.Recommendation;
import com.fitnes.aiservice.repository.RecommendationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    public List<Recommendation> getUserRecommendation(String userId) {
        return recommendationRepository.findByUserId(userId);
    }

    public List<Recommendation> getActivityRecommenation(String activityId) {
        return Collections.singletonList(recommendationRepository.findByActivityId(activityId)
                .orElseThrow(() -> new RuntimeException("no Recommendation found from this id " + activityId)));
    }
}
