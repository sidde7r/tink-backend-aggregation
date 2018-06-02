package se.tink.backend.insights.core.domain.contents;

import com.google.common.base.MoreObjects;

public class MonthlySummaryInsightCategoryData {

    private double amount;
    private String category;
    private int count;
    private double average;

    public MonthlySummaryInsightCategoryData(String category, double amount, int count, double average) {
        this.category = category;
        this.amount = amount;
        this.count = count;
        this.average = average;
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

    public double getAverage() {
        return average;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("categoryId", category).add("amount", amount).add("count", count)
                .add("amount", amount)
                .toString();
    }

}
