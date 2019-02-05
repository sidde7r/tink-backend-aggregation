package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

public class DateValueEntity {
    private String formattedDate;
    private long rawValue;

    public long getRawValue() {
        return rawValue;
    }

    public void setRawValue(long rawValue) {
        this.rawValue = rawValue;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }
}
