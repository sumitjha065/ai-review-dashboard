package com.reviewdashboard.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reviewdashboard.dto.SentimentResult;
import com.reviewdashboard.dto.SummaryResult;
import com.reviewdashboard.entity.AnalysisSummary;
import com.reviewdashboard.entity.Review;
import com.reviewdashboard.exception.ResourceNotFoundException;
import com.reviewdashboard.repository.AnalysisSummaryRepository;
import com.reviewdashboard.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@lombok.extern.slf4j.Slf4j
@Service
public class ReviewProcessingService {

    // Repository to store individual reviews
    private final ReviewRepository reviewRepository;

    // Repository to store final analysis summary
    private final AnalysisSummaryRepository summaryRepository;

    // Gemini service for sentiment and summary generation
    private final GeminiServiceImpl geminiService;

    // Used to convert objects to JSON
    private final ObjectMapper objectMapper;

    public ReviewProcessingService(ReviewRepository reviewRepository,
                                   AnalysisSummaryRepository summaryRepository,
                                   GeminiServiceImpl geminiService,
                                   ObjectMapper objectMapper) {
        this.reviewRepository = reviewRepository;
        this.summaryRepository = summaryRepository;
        this.geminiService = geminiService;
        this.objectMapper = objectMapper;
    }

    // Handles CSV file upload and triggers async analysis
    @Transactional
    public String processFileUpload(MultipartFile file) {
        String batchId = UUID.randomUUID().toString();
        List<Review> reviews = new ArrayList<>();

        int rowNum = 0;
        try (java.io.BufferedReader reader =
                     new java.io.BufferedReader(new InputStreamReader(file.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                rowNum++;

                // Skip CSV header
                if (rowNum == 1)
                    continue;

                // Split CSV row safely (handles quoted commas)
                String[] row = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                if (row.length > 0 && !row[0].isBlank()) {
                    String text = row[0];

                    // Remove surrounding quotes if present
                    if (text.startsWith("\"") && text.endsWith("\"")) {
                        text = text.substring(1, text.length() - 1);
                    }

                    // Create review entity with PENDING sentiment
                    reviews.add(Review.builder()
                            .reviewText(text)
                            .productId("UNKNOWN")
                            .batchId(batchId)
                            .sentiment("PENDING")
                            .build());
                }
            }

            // Save all reviews first
            reviewRepository.saveAll(reviews);

            // Run analysis asynchronously
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    analyzeBatch(batchId, reviews);
                } catch (Exception e) {
                    log.error("Async Analysis failed for batch: {}", batchId, e);
                }
            });

            return batchId;
        } catch (Exception e) {
            log.error("CRITICAL ERROR in processFileUpload", e);
            throw new RuntimeException("Failed to process CSV file: " + e.getMessage(), e);
        }
    }

    // Fetch final analysis summary by batch ID
    public AnalysisSummary getAnalysisResults(String batchId) {
        return summaryRepository.findByBatchId(batchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Analysis not found for batch: " + batchId));
    }

    // Performs sentiment analysis and summary generation
    private void analyzeBatch(String batchId, List<Review> reviews) {
        try {
            int batchSize = 1000;

            // Process reviews in smaller batches
            for (int i = 0; i < reviews.size(); i += batchSize) {
                int end = Math.min(reviews.size(), i + batchSize);
                List<Review> batch = reviews.subList(i, end);

                List<String> batchTexts = batch.stream()
                        .map(Review::getReviewText)
                        .collect(Collectors.toList());

                try {
                    // Call Gemini for sentiment analysis
                    java.util.Map<String, SentimentResult> results =
                            geminiService.analyzeSentimentBatch(batchTexts);

                    // Update sentiment for each review
                    for (Review review : batch) {
                        SentimentResult res = results.get(review.getReviewText());
                        if (res != null) {
                            review.setSentiment(res.getSentiment());
                        } else {
                            review.setSentiment("NEUTRAL");
                        }
                    }
                } catch (Exception e) {
                    log.error("Batch analysis failed for batch starting at index {}", i, e);
                    batch.forEach(r -> r.setSentiment("NEUTRAL"));
                }

                // Save updated sentiments
                reviewRepository.saveAll(batch);
            }

            // Generate overall summary
            List<String> allTexts = reviews.stream()
                    .map(Review::getReviewText)
                    .collect(Collectors.toList());

            SummaryResult summaryResult = geminiService.generateSummary(allTexts);

            // Count sentiment distribution
            long positive = reviews.stream()
                    .filter(r -> "POSITIVE".equals(r.getSentiment()))
                    .count();
            long neutral = reviews.stream()
                    .filter(r -> "NEUTRAL".equals(r.getSentiment()))
                    .count();
            long negative = reviews.stream()
                    .filter(r -> "NEGATIVE".equals(r.getSentiment()))
                    .count();

            try {
                // Save final analysis summary
                AnalysisSummary summary = AnalysisSummary.builder()
                        .batchId(batchId)
                        .totalReviews(reviews.size())
                        .positiveCount(positive)
                        .neutralCount(neutral)
                        .negativeCount(negative)
                        .overallSummary(summaryResult.getSummary())
                        .topProsJson(objectMapper.writeValueAsString(summaryResult.getPros()))
                        .topConsJson(objectMapper.writeValueAsString(summaryResult.getCons()))
                        .build();

                summaryRepository.save(summary);
            } catch (Exception e) {
                log.error("Failed to save summary JSON", e);
            }
        } catch (Exception e) {
            log.error("Analysis Process Failed: {}", e.getMessage(), e);

            // Save failure summary if analysis completely fails
            try {
                AnalysisSummary errorSummary = AnalysisSummary.builder()
                        .batchId(batchId)
                        .totalReviews(reviews.size())
                        .positiveCount(0L)
                        .neutralCount(0L)
                        .negativeCount(0L)
                        .overallSummary("FAILED: " + e.getMessage())
                        .topProsJson("[]")
                        .topConsJson("[]")
                        .build();

                summaryRepository.save(errorSummary);
            } catch (Exception saveErr) {
                saveErr.printStackTrace();
            }
        }
    }
}
