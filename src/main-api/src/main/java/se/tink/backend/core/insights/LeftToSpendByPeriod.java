package se.tink.backend.core.insights;

import io.protostuff.Tag;

public class LeftToSpendByPeriod {
    @Tag(1)
    private String period;
    @Tag(2)
    private Double percentage;

    public LeftToSpendByPeriod() {
    }

    public LeftToSpendByPeriod(String period, Double percentage) {
        this.period = period;
        this.percentage = percentage;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}
