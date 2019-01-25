package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity extends Amount {

    @JsonProperty("currency")
    public void setCurrency(String currency) {
        super.setCurrency(currency);
    }

    @JsonProperty("balance")
    public void setBalance(double amount) {
        super.setValue(amount);
    }

    @JsonProperty("amount")
    public void setAmount(double amount) {
        super.setValue(amount);
    }

    @JsonProperty("market_value")
    public void setValue(double amount) {
        super.setValue(amount);
    }
}
