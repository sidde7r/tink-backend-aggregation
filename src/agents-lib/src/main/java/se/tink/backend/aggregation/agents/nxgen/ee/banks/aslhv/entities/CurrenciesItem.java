package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CurrenciesItem {

    @JsonProperty("name")
    private String name;

    @JsonProperty("currency_id")
    private int currencyId;

    @JsonIgnore
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    @JsonIgnore
    public int getCurrencyId() {
        return currencyId;
    }
}
