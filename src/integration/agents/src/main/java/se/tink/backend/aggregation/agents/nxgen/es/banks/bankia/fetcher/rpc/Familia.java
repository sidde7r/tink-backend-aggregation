package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Familia {

    @JsonProperty("idFamilia")
    private String idFamilia;

    @JsonProperty("idSubfamilia")
    private String idSubfamilia;

    public String getIdFamilia() {
        return idFamilia;
    }

    public String getIdSubfamilia() {
        return idSubfamilia;
    }
}
