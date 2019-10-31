package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountDetailsEntity {

    @JsonProperty("Key")
    private String key;

    @JsonProperty("Value")
    private String value;

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
