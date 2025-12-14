package com.reviewdashboard.dto;

import java.util.List;

public class SummaryResult {
    private List<String> pros;
    private List<String> cons;
    private String summary;

    public SummaryResult() {}

    public SummaryResult(List<String> pros, List<String> cons, String summary) {
        this.pros = pros;
        this.cons = cons;
        this.summary = summary;
    }

    public List<String> getPros() { return pros; }
    public void setPros(List<String> pros) { this.pros = pros; }

    public List<String> getCons() { return cons; }
    public void setCons(List<String> cons) { this.cons = cons; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<String> pros;
        private List<String> cons;
        private String summary;

        public Builder pros(List<String> pros) { this.pros = pros; return this; }
        public Builder cons(List<String> cons) { this.cons = cons; return this; }
        public Builder summary(String summary) { this.summary = summary; return this; }

        public SummaryResult build() {
            return new SummaryResult(pros, cons, summary);
        }
    }
}
