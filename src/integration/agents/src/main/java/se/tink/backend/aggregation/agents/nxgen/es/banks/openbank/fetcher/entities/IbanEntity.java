package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IbanEntity {
    @JsonProperty("codbban")
    private String iban;

    @JsonProperty("digitodecontrol")
    private String checkDigits;

    @JsonProperty("pais")
    private String country;

    public String getIban() {
        return iban;
    }

    public String getCheckDigits() {
        return checkDigits;
    }

    public String getCountry() {
        return country;
    }
}
