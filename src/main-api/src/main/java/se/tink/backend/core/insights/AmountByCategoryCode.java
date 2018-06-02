package se.tink.backend.core.insights;

import io.protostuff.Tag;

public class AmountByCategoryCode {
    @Tag(1)
    private String categoryCode;
    @Tag(2)
    private Double amount;

    public AmountByCategoryCode() {
    }

    public AmountByCategoryCode(String categoryCode, Double amount) {
        this.categoryCode = categoryCode;
        this.amount = amount;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
