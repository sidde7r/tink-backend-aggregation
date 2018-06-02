package se.tink.backend.system.document.core;

public class Salary {
    private final String date;
    private final String amount;
    private final String description;

    public Salary(String date, String amount, String description) {
        this.date = date;
        this.amount = amount;
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public String getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }
}
