package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class CurrenciesItem {

    @JsonProperty("name")
    private String name;

    @JsonProperty("currency_id")
    private int currencyId;

    public String getName() {
        return name;
    }

    public int getCurrencyId() {
        return currencyId;
    }
}
