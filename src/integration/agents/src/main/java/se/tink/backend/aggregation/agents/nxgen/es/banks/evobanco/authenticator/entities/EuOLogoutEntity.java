package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EuOLogoutEntity {
    @JsonProperty("codigoRetorno")
    private String returnCode;

    public String getReturnCode() {
        return returnCode;
    }
}
