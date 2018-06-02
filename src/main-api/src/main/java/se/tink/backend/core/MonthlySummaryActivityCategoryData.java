package se.tink.backend.core;

import com.google.common.base.MoreObjects;

public class MonthlySummaryActivityCategoryData {
    private double amount;
    private String categoryId;
    private int count;
	private double average;

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
    
	public void setAverage(double averag) {
		this.average = averag;
	}
	
	public double getAverage()
	{
		return average;
	}

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("categoryId", categoryId).add("amount", amount).add("count", count).add("amount", amount)
                .toString();
    }


}
