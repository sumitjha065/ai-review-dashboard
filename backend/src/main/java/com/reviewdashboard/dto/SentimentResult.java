package com.reviewdashboard.dto;

public class SentimentResult {
    private String sentiment; // POSITIVE, NEUTRAL, NEGATIVE

    public SentimentResult() {}

    public SentimentResult(String sentiment) {
        this.sentiment = sentiment;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String sentiment;

        public Builder sentiment(String sentiment) {
            this.sentiment = sentiment;
            return this;
        }

        public SentimentResult build() {
            return new SentimentResult(sentiment);
        }
    }
}
