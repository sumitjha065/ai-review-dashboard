package com.reviewdashboard.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_summary")
public class AnalysisSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String batchId;

    @Column(columnDefinition = "TEXT")
    private String topProsJson; // Stored as JSON String: ["Battery", "Screen"]

    @Column(columnDefinition = "TEXT")
    private String topConsJson; // Stored as JSON String: ["Price", "Overheating"]

    @Column(columnDefinition = "TEXT")
    private String overallSummary;

    private long totalReviews;
    private long positiveCount;
    private long neutralCount;
    private long negativeCount;

    @CreationTimestamp
    private LocalDateTime analyzedAt;

    public AnalysisSummary() {}

    public AnalysisSummary(Long id, String batchId, String topProsJson, String topConsJson, String overallSummary, long totalReviews, long positiveCount, long neutralCount, long negativeCount, LocalDateTime analyzedAt) {
        this.id = id;
        this.batchId = batchId;
        this.topProsJson = topProsJson;
        this.topConsJson = topConsJson;
        this.overallSummary = overallSummary;
        this.totalReviews = totalReviews;
        this.positiveCount = positiveCount;
        this.neutralCount = neutralCount;
        this.negativeCount = negativeCount;
        this.analyzedAt = analyzedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public String getTopProsJson() { return topProsJson; }
    public void setTopProsJson(String topProsJson) { this.topProsJson = topProsJson; }

    public String getTopConsJson() { return topConsJson; }
    public void setTopConsJson(String topConsJson) { this.topConsJson = topConsJson; }

    public String getOverallSummary() { return overallSummary; }
    public void setOverallSummary(String overallSummary) { this.overallSummary = overallSummary; }

    public long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(long totalReviews) { this.totalReviews = totalReviews; }

    public long getPositiveCount() { return positiveCount; }
    public void setPositiveCount(long positiveCount) { this.positiveCount = positiveCount; }

    public long getNeutralCount() { return neutralCount; }
    public void setNeutralCount(long neutralCount) { this.neutralCount = neutralCount; }

    public long getNegativeCount() { return negativeCount; }
    public void setNegativeCount(long negativeCount) { this.negativeCount = negativeCount; }

    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String batchId;
        private String topProsJson;
        private String topConsJson;
        private String overallSummary;
        private long totalReviews;
        private long positiveCount;
        private long neutralCount;
        private long negativeCount;
        private LocalDateTime analyzedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder batchId(String batchId) { this.batchId = batchId; return this; }
        public Builder topProsJson(String topProsJson) { this.topProsJson = topProsJson; return this; }
        public Builder topConsJson(String topConsJson) { this.topConsJson = topConsJson; return this; }
        public Builder overallSummary(String overallSummary) { this.overallSummary = overallSummary; return this; }
        public Builder totalReviews(long totalReviews) { this.totalReviews = totalReviews; return this; }
        public Builder positiveCount(long positiveCount) { this.positiveCount = positiveCount; return this; }
        public Builder neutralCount(long neutralCount) { this.neutralCount = neutralCount; return this; }
        public Builder negativeCount(long negativeCount) { this.negativeCount = negativeCount; return this; }
        public Builder analyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; return this; }

        public AnalysisSummary build() {
            return new AnalysisSummary(id, batchId, topProsJson, topConsJson, overallSummary, totalReviews, positiveCount, neutralCount, negativeCount, analyzedAt);
        }
    }
}
