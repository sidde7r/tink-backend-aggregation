package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CurrencyEntity {
    @JsonProperty("Code")
    private String code;

    public String getCode() {
        return code;
    }
}
