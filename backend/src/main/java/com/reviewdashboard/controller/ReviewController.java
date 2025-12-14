package com.reviewdashboard.controller;

import com.reviewdashboard.entity.AnalysisSummary;
import com.reviewdashboard.service.impl.ReviewProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewProcessingService processingService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String batchId = processingService.processFileUpload(file);
        return ResponseEntity.ok(Map.of("batchId", batchId, "message", "File uploaded and analysis started."));
    }

    @GetMapping("/analysis/{batchId}")
    public ResponseEntity<?> getAnalysis(@PathVariable String batchId) {
        try {
            AnalysisSummary summary = processingService.getAnalysisResults(batchId);
            return ResponseEntity.ok(summary);
        } catch (com.reviewdashboard.exception.ResourceNotFoundException e) {
            // If not found, assume it's pending (Simple Fix for Clean Console)
            return ResponseEntity.status(org.springframework.http.HttpStatus.ACCEPTED)
                    .body(Map.of("status", "PENDING", "message", "Analysis in progress"));
        }
    }
}
