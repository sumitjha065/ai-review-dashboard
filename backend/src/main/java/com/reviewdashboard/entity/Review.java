package com.reviewdashboard.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_sentiment", columnList = "sentiment"),
        @Index(name = "idx_batch_id", columnList = "batch_id")
})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reviewText;

    private String productId;

    // We store the simple string value: POSITIVE, NEUTRAL, NEGATIVE
    @Column(length = 20)
    private String sentiment;

    // JSON string for specific themes/keywords if needed per review
    @Column(columnDefinition = "TEXT")
    private String themesJson;

    // Link to a specific upload batch
    @Column(name = "batch_id")
    private String batchId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Review() {}

    public Review(Long id, String reviewText, String productId, String sentiment, String themesJson, String batchId, LocalDateTime createdAt) {
        this.id = id;
        this.reviewText = reviewText;
        this.productId = productId;
        this.sentiment = sentiment;
        this.themesJson = themesJson;
        this.batchId = batchId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }

    public String getThemesJson() { return themesJson; }
    public void setThemesJson(String themesJson) { this.themesJson = themesJson; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String reviewText;
        private String productId;
        private String sentiment;
        private String themesJson;
        private String batchId;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder reviewText(String reviewText) { this.reviewText = reviewText; return this; }
        public Builder productId(String productId) { this.productId = productId; return this; }
        public Builder sentiment(String sentiment) { this.sentiment = sentiment; return this; }
        public Builder themesJson(String themesJson) { this.themesJson = themesJson; return this; }
        public Builder batchId(String batchId) { this.batchId = batchId; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Review build() {
            return new Review(id, reviewText, productId, sentiment, themesJson, batchId, createdAt);
        }
    }
}
