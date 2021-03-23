package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BalanceAmount {

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("currency")
    private String currency;

    @Override
    public String toString() {
        return "BalanceAmount{"
                + "amount = '"
                + amount
                + '\''
                + ",currency = '"
                + currency
                + '\''
                + "}";
    }
}
