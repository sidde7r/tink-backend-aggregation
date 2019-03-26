package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Entrada {

    @JsonProperty("contractID")
    private String contractID;

    @JsonProperty("lang")
    private Lang lang;

    @JsonProperty("pan")
    private String pan;

    @JsonProperty("indMPX")
    private String indMPX;

    public Entrada(String contractID, Lang lang, String pan, String indMPX) {
        this.contractID = contractID;
        this.lang = lang;
        this.pan = pan;
        this.indMPX = indMPX;
    }
}
