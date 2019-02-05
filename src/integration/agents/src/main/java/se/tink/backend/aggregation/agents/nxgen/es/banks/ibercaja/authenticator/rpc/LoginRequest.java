package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest {

    @JsonProperty("NICI")
    private int nici;

    public LoginRequest(int nici) {

        this.nici = nici;
    }
}


