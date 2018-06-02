package se.tink.backend.insights.core.domain.contents;

public class WeeklySummaryInsightCategoryData {

    private double amount;
    private String category;
    private int count;

    public WeeklySummaryInsightCategoryData(String category, double amount, int count) {
        this.category = category;
        this.amount = amount;
        this.count = count;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public int getCount() {
        return count;
    }
}
