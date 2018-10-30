package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionItem {

    @JsonProperty("date")
    private Date date;

    @JsonProperty("amount")
    private double amount;

    @JsonProperty("description")
    private String description;

    @JsonProperty("is_completed")
    private boolean completed;

    @JsonProperty("currency_id")
    private int currencyId;

    public Date getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getCurrencyId() {
        return currencyId;
    }
}
