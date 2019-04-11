package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountIdentifierEntity {
    @JsonProperty("pais")
    private String country;

    @JsonProperty("digitosDeControl")
    private String controlDigits;

    @JsonProperty("identificador")
    private String identifier;

    public void setCountry(String country) {
        this.country = country;
    }

    public void setControlDigits(String controlDigits) {
        this.controlDigits = controlDigits;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
