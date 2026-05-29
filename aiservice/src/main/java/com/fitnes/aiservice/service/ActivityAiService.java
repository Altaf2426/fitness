package com.fitnes.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitnes.aiservice.model.Activity;
import com.fitnes.aiservice.model.Recommendation;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Collections.replaceAll;


@Service
@Slf4j
@RequiredArgsConstructor

public class ActivityAiService {
    private final GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getRecommendation(prompt);
        log.info("Response from Ai {} " , aiResponse);
        return processAiResponse(activity , aiResponse);

    }

    private Recommendation processAiResponse(Activity activity, String aiResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(aiResponse);
            JsonNode textNode = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            String jsonContent = textNode.asText()
                    .replaceAll("```json\\n", "")
                    .replaceAll("\\n```", "")
                    .replaceAll("^```json", "")
                     .replaceAll("^```", "")
                     .replaceAll("```$", "")
                    .trim();


            JsonNode analysisJson = mapper.readTree(jsonContent);
            JsonNode analysisNode = analysisJson.path("analysis");
            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis , analysisNode , "overall" , "Overall:");
            addAnalysisSection(fullAnalysis , analysisNode , "pace" , "Pace:");
            addAnalysisSection(fullAnalysis , analysisNode , "heartRate" , "Heart Rate:");
            addAnalysisSection(fullAnalysis , analysisNode , "caloriesBurned" , "Calories Burned:");


            List<String> improvements = extractImprovement(analysisJson.path("improvements"));
            List<String> suggestion = extractSuggestion(analysisJson.path("suggestion"));
            List<String> safety = extractSaftyGuideLine(analysisJson.path("safety"));

            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .type(activity.getType().toString())
                    .recommendation(fullAnalysis.toString().trim())
                    .improvement(improvements)
                    .safety(safety)
                    .suggestion(suggestion)
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return createDefaultRecommendation(activity);
        }
    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .type(activity.getType().toString())
                .recommendation("Unable to generate detailed analysis")
                .improvement(Collections.singletonList("Continue with your daily routine"))
                .safety(Collections.singletonList("Consider consulting a fitness consultant"))
                .suggestion(Arrays.asList(
                        "always warm up before starting workout",
                        "stay hydrated not only during workout during wholeday",
                        ""
                ))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<String> extractSaftyGuideLine(JsonNode safetyNode) {
        List<String> safety = new ArrayList<>();
        if (safetyNode.isArray()) {
            safetyNode.forEach(item -> safety.add(item.asText()));
        }
        return safety.isEmpty() ?
                Collections.singletonList("Follow General Safety Guideline") :
                safety;
    }

    private List<String> extractSuggestion(JsonNode suggestionNode) {
        List<String> suggestion = new ArrayList<>();
        if (suggestionNode.isArray()) {
            suggestionNode.forEach(imporvement -> {
                String workout = imporvement.path("workout").asText();
                String description = imporvement.path("description").asText();
                suggestion.add(String.format("%s: %s"  , workout , description));
            });

        }
        return suggestion.isEmpty() ?
                Collections.singletonList("No Specific suggestion provided") :
                suggestion;
    }

    private List<String> extractImprovement(JsonNode improvementNode) {
        List<String> imporvements = new ArrayList<>();
        if (improvementNode.isArray()) {
            improvementNode.forEach(improvement -> {
                String area = improvement.path("area").asText();
                String detail = improvement.path("recommendation").asText();
                imporvements.add(String.format("%s: %s"  , area , detail));
            });

        }
        return imporvements.isEmpty() ?
                Collections.singletonList("No Specific Improvement Needed") :
                imporvements;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if(!analysisNode.path(key).isMissingNode()){
            fullAnalysis.append(prefix).
                    append(analysisNode.path(key).asText())
                    .append("\n\n");
        }
    }


    private String createPromptForActivity(Activity activity) {
        return String.format("""
You are a certified AI Fitness and Nutrition Coach.

Analyze the user's structured activity data and provide SAFE and personalized recommendations.

User Activity Data:
- Activity Type: %s
- Duration: %s minutes
- Calories Burned: %s kcal
- Additional Metrics: %s

Return ONLY valid JSON in this format:

{
  "activityLevel": "Beginner | Intermediate | Advanced",
  "fitnessScore": number,
  "caloriesBurnEstimate": number,

  "recommendedWorkout": [
    {
      "name": "exercise name",
      "duration": "minutes",
      "intensity": "Low | Medium | High",
      "frequency": "times per week"
    }
  ],

  "dietPlan": {
    "goal": "Weight Gain | Weight Loss | Maintenance",
    "recommendedCalories": number,
    "macros": {
      "protein": "grams",
      "carbs": "grams",
      "fats": "grams"
    },
    "meals": [
      "meal suggestion 1",
      "meal suggestion 2"
    ]
  },

  "hydrationAdvice": "daily water intake",
  "sleepAdvice": "recommended sleep hours",

  "safetyGuidelines": [
    "important safety rule",
    "injury prevention tip"
  ],

  "warnings": [
    "any health risk"
  ],

  "improvements": [
    "what to improve"
  ],

  "finalAdvice": "short summary"
}

Rules:
- Return ONLY JSON
- Keep recommendations safe and realistic
- Use activity data strictly for decisions
- If data is missing, give general safe advice
""",
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );
    }
}
