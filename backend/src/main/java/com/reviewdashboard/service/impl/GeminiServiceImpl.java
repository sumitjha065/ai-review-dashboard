package com.reviewdashboard.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reviewdashboard.dto.SentimentResult;
import com.reviewdashboard.dto.SummaryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@lombok.extern.slf4j.Slf4j
@Service
public class GeminiServiceImpl {

    // Gemini API key (loaded from environment or properties)
    @Value("${gemini.api.key}")
    private String apiKey;

    // Base URL for Gemini APIs
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta";

    // Cached resolved model endpoint
    private String cachedEndpointUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        // Configure RestTemplate with timeout to avoid hanging requests
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(120000); // 120 seconds
        factory.setReadTimeout(120000);    // 120 seconds

        this.restTemplate = new RestTemplate(factory);
    }

    // Single review sentiment analysis (mainly for testing)
    public SentimentResult analyzeSentiment(String text) {
        return analyzeSentimentBatch(List.of(text))
                .getOrDefault(text,
                        SentimentResult.builder().sentiment("NEUTRAL").build());
    }

    // Batch sentiment analysis for multiple reviews
    public Map<String, SentimentResult> analyzeSentimentBatch(List<String> reviews) {
        if (reviews.isEmpty())
            return Collections.emptyMap();

        // Build prompt for batch sentiment classification
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Classify the sentiment of the following reviews as POSITIVE, NEUTRAL, or NEGATIVE.\n");
        promptBuilder.append("Return strictly a JSON array of objects, where each object has 'sentiment' field.\n");
        promptBuilder.append("The order must match the input list exactly.\n");
        promptBuilder.append("Reviews:\n");

        for (int i = 0; i < reviews.size(); i++) {
            promptBuilder.append(i)
                    .append(". ")
                    .append(reviews.get(i).replace("\n", " "))
                    .append("\n");
        }

        try {
            String responseText = callGemini(promptBuilder.toString());
            return parseSentimentBatch(responseText, reviews);
        } catch (Exception e) {
            log.error("Error analyzing sentiment batch", e);

            // Fallback: mark all reviews as NEUTRAL
            Map<String, SentimentResult> fallback = new HashMap<>();
            for (String r : reviews) {
                fallback.put(r,
                        SentimentResult.builder().sentiment("NEUTRAL").build());
            }
            return fallback;
        }
    }

    // Generate summary (pros, cons, overall summary)
    public SummaryResult generateSummary(List<String> reviews) {
        // Limit reviews to avoid token limit issues
        List<String> limitedReviews = reviews.subList(0, Math.min(reviews.size(), 50));
        String reviewsText = String.join("\n- ", limitedReviews);

        String prompt =
                "Analyze the following list of reviews. If they are just product names, list them as features. " +
                "Identify top 5 pros and top 5 cons. Write a short overall summary. " +
                "Return strictly valid JSON (NO markdown backticks) with format: " +
                "{\"pros\": [], \"cons\": [], \"summary\": \"...\"}. Reviews:\n" +
                reviewsText;

        try {
            String responseText = callGemini(prompt);
            return parseSummary(responseText);
        } catch (Exception e) {
            log.error("Error generating summary", e);

            // Safe fallback response
            return SummaryResult.builder()
                    .pros(Collections.emptyList())
                    .cons(Collections.emptyList())
                    .summary("Could not generate summary due to API error: " + e.getMessage())
                    .build();
        }
    }

    // Calls Gemini API with retry and rate-limit handling
    private String callGemini(String inputPrompt) {
        ensureModelUrlResolved();

        int maxRetries = 5;
        int delayMs = 2000;

        for (int i = 0; i < maxRetries; i++) {
            try {
                return executeRequest(cachedEndpointUrl, inputPrompt);
            } catch (HttpClientErrorException.TooManyRequests e) {
                // Handle Gemini rate limiting (HTTP 429)
                log.warn("Gemini rate limit hit. Retrying in {}ms... (Attempt {}/{})",
                        delayMs, i + 1, maxRetries);
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry wait", ie);
                }
                delayMs = Math.min(delayMs * 2, 8000);
            } catch (Exception e) {
                if (i == maxRetries - 1) {
                    log.error("Gemini request failed after retries", e);
                    throw new RuntimeException("Failed to call Gemini API after retries", e);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }

        throw new RuntimeException("Gemini API failed after " + maxRetries + " attempts");
    }

    // Executes the actual HTTP request to Gemini
    private String executeRequest(String url, String inputPrompt) {
        String fullUrl = url + "?key=" + apiKey;

        Map<String, Object> contentPart = Map.of("text", inputPrompt);
        Map<String, Object> content = Map.of("parts", List.of(contentPart));

        // Lower temperature for more stable JSON output
        Map<String, Object> generationConfig = Map.of(
                "temperature", 0.2,
                "topP", 0.85
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(content),
                "generationConfig", generationConfig
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String response = restTemplate.postForObject(fullUrl, entity, String.class);
            JsonNode root = objectMapper.readTree(response);

            // Extract generated text from response
            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            log.error("Gemini request execution failed. URL: {}", url, e);
            throw new RuntimeException("Failed to execute or parse Gemini response", e);
        }
    }

    // Utility method to clean malformed or wrapped JSON from AI output
    private String cleanJson(String raw) {
        if (raw == null)
            return "{}";

        raw = raw.trim();

        // Try to extract JSON from markdown code block
        java.util.regex.Pattern pattern =
                java.util.regex.Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```");
        java.util.regex.Matcher matcher = pattern.matcher(raw);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // Try extracting JSON object or array directly
        int firstBrace = raw.indexOf('{');
        int firstBracket = raw.indexOf('[');
        int start = -1;

        if (firstBrace != -1 && firstBracket != -1) {
            start = Math.min(firstBrace, firstBracket);
        } else if (firstBrace != -1) {
            start = firstBrace;
        } else if (firstBracket != -1) {
            start = firstBracket;
        }

        if (start != -1) {
            int endBrace = raw.lastIndexOf('}');
            int endBracket = raw.lastIndexOf(']');
            int end = Math.max(endBrace, endBracket);

            if (end > start) {
                return raw.substring(start, end + 1);
            }
        }

        // Last fallback: replace single quotes with double quotes
        if (!raw.contains("\"") && raw.contains("'")) {
            return raw.replace("'", "\"");
        }

        return raw;
    }
}
