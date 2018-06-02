package se.tink.backend.core.insights;

import io.protostuff.Tag;

public class AmountByWeekday {
    @Tag(1)
    private String weekday;
    @Tag(2)
    private Double amount;

    public AmountByWeekday() {
    }

    public AmountByWeekday(String weekday, Double amount) {
        this.amount = amount;
        this.weekday = weekday;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }
}
