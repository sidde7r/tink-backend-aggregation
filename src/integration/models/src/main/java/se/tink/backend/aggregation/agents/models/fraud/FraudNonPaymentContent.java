package se.tink.backend.aggregation.agents.models.fraud;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class FraudNonPaymentContent extends FraudDetailsContent {

    private String name;
    private double amount;
    private String type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String generateContentId() {
        return String.valueOf(
                Objects.hash(
                        itemType(),
                        name,
                        amount,
                        type,
                        Optional.ofNullable(date).orElse(new Date(0))));
    }

    @Override
    public FraudTypes itemType() {
        return FraudTypes.CREDIT;
    }
}
