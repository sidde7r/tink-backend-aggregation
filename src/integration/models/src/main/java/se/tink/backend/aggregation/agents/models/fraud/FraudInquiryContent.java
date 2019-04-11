package se.tink.backend.aggregation.agents.models.fraud;

import java.util.Date;
import java.util.Objects;

public class FraudInquiryContent extends FraudDetailsContent {

    private String name;
    private double amount;
    private Date date;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String generateContentId() {
        return String.valueOf(Objects.hash(itemType(), name, date));
    }

    @Override
    public FraudTypes itemType() {
        return FraudTypes.INQUIRY;
    }
}
