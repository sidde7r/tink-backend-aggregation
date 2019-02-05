package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity extends Amount {

    public BalanceEntity(@JsonProperty("moneda") String currency, @JsonProperty("importe") double value) {
        super(currency, value);
    }
}
