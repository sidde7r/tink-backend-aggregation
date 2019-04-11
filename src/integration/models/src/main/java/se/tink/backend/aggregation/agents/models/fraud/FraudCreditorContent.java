package se.tink.backend.aggregation.agents.models.fraud;

import java.util.Date;
import java.util.Objects;

public class FraudCreditorContent extends FraudDetailsContent {

    private int number;
    private double amount;
    private Date registered;

    public Date getRegistered() {
        return registered;
    }

    public void setRegistered(Date registered) {
        this.registered = registered;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String generateContentId() {
        return String.valueOf(Objects.hash(itemType(), number, amount));
    }

    @Override
    public FraudTypes itemType() {
        return FraudTypes.CREDIT;
    }
}
