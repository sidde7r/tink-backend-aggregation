package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.Amount;

@JsonObject
public class BalanceEntity {

    @JsonProperty("importe")
    private double value;

    @JsonProperty("moneda")
    private String currency;

    @JsonIgnore
    public Amount toTinkAmount(){
        return new Amount(currency, value);
    }
}
