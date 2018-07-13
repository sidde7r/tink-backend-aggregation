package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NfcCardEntity {
    private boolean isNfc;
    private boolean isActive;
    private String expirationDate;

    @JsonProperty("isNfc")
    public boolean isNfc() {
        return isNfc;
    }

    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }

    public String getExpirationDate() {
        return expirationDate;
    }
}

