package se.tink.backend.core.insights;

import io.protostuff.Tag;

public class InsightsResponse {
    @Tag(1)
    private Categories categories;
    @Tag(2)
    private Mortgage mortgage;
    @Tag(3)
    private Savings savings;
    @Tag(4)
    private DailySpending dailySpending;
    @Tag(5)
    private LeftToSpend leftToSpend;

    public Categories getCategories() {
        return categories;
    }

    public void setCategories(Categories categories) {
        this.categories = categories;
    }

    public Mortgage getMortgage() {
        return mortgage;
    }

    public void setMortgage(Mortgage mortgage) {
        this.mortgage = mortgage;
    }

    public Savings getSavings() {
        return savings;
    }

    public void setSavings(Savings savings) {
        this.savings = savings;
    }

    public DailySpending getDailySpending() {
        return dailySpending;
    }

    public void setDailySpending(DailySpending dailySpending) {
        this.dailySpending = dailySpending;
    }

    public LeftToSpend getLeftToSpend() {
        return leftToSpend;
    }

    public void setLeftToSpend(LeftToSpend leftToSpend) {
        this.leftToSpend = leftToSpend;
    }
}
