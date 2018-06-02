package se.tink.backend.common.workers.activity.generators.models;

import com.google.common.base.MoreObjects;

public class WeeklySummaryActivityCategoryData {
    private double amount;
    private String categoryId;
    private int count;

    public double getAmount() {
        return amount;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public int getCount() {
        return count;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("categoryId", categoryId).add("amount", amount).add("count", count)
                .toString();
    }
}
