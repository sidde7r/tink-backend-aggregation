package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitialParameters {
    @JsonProperty("param-tabel")
    private ParamTable paramTable;

    public ParamTable getParamTable() {
        return paramTable;
    }
}
