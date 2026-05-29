package com.fitnes.aiservice.service;

import com.fitnes.aiservice.model.Activity;
import com.fitnes.aiservice.model.Recommendation;
import com.fitnes.aiservice.repository.RecommendationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor

public class ActivityMessageListener {
    private final ActivityAiService activityAiService;
    private final RecommendationRepository recommendationRepository;

    @KafkaListener(topics = "${kafka.topic.name}" , groupId = "activity-processor-group-v2")
    public void processActivity(Activity activity){
        log.info("Received Activity for Processing : {}" , activity.getUserId());
        Recommendation recommendation = activityAiService.generateRecommendation(activity);
        recommendationRepository.save(recommendation);
    }
}
