package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionItem {

    @JsonProperty("amount")
    private double amount;

    @JsonProperty("is_completed")
    private boolean completed;

    @JsonProperty("currency_id")
    private int currencyId;

    @JsonProperty("date")
    private Date date;

    @JsonProperty("description")
    private String description;

    public double getAmount() {
        return amount;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    public boolean isCompleted() {
        return completed;
    }

    @JsonIgnore
    public Optional<Date> getDate() {
        return Optional.ofNullable(date);
    }

    @JsonIgnore
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }
}
