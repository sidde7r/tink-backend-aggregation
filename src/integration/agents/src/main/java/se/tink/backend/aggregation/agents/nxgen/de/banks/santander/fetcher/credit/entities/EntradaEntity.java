package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EntradaEntity {

    @JsonProperty("lang")
    private LangEntity lang;

    @JsonProperty("contractID")
    private String contractID;

    @JsonProperty("pan")
    private String pan;

    public EntradaEntity(String contractID, LangEntity lang, String pan) {
        this.contractID = contractID;
        this.lang = lang;
        this.pan = pan;
    }
}
