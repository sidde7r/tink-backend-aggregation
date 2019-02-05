package se.tink.backend.aggregation.agents.banks.se.collector.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Date;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.date.DateUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    private double amount;
    private Date date;
    private String description;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @JsonProperty("Amount")
    public void setAmountCaps(double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @JsonProperty("Date")
    public void setDateCaps(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("Description")
    public void setDescriptionCaps(String description) {
        this.description = description;
    }

    public Transaction toTinkTransaction() {
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDate(DateUtils.flattenTime(date));
        transaction.setDescription(description);

        return transaction;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Amount", amount)
                .add("Date", date)
                .add("Description", description)
                .toString();
    }
}
