package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

public class DoubleValueEntity {
    private double rawValue;
    private String formattedAmount;

    public String getFormattedAmount() {
        return formattedAmount;
    }

    public void setFormattedAmount(String formattedAmount) {
        this.formattedAmount = formattedAmount;
    }

    public double getRawValue() {
        return rawValue;
    }

    public void setRawValue(double rawValue) {
        this.rawValue = rawValue;
    }
}
