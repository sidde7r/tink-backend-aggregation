package se.tink.backend.aggregation.agents.creditcards.coop.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.date.DateUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {

    @JsonProperty("Amount")
    private Double amount;

    @JsonProperty("Date")
    private String date;

    @JsonProperty("Text")
    private String description;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private Date getDateFlattened() {
        if (Strings.isNullOrEmpty(date)) {
            return null;
        }

        String dateStringWithoutTimeZone = date.substring(0, date.indexOf("+"));
        String dateStringInMillis = dateStringWithoutTimeZone.replaceAll("[^0-9]", "");

        Date date = new Date(Long.parseLong(dateStringInMillis));

        return DateUtils.flattenTime(date);
    }

    public Transaction toTransaction() {
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDate(getDateFlattened());
        transaction.setDescription(description);

        return transaction;
    }
}
