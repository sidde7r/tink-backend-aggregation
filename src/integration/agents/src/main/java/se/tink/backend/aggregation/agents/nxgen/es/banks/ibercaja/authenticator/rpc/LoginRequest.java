package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest {

    @JsonProperty("NICI")
    private String nici;

    public LoginRequest(String nici) {

        this.nici = nici;
    }
}
