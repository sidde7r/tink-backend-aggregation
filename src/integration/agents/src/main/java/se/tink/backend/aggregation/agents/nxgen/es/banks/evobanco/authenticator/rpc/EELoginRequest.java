package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EeILoginEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EELoginRequest {
    @JsonProperty("EE_I_Login")
    private EeILoginEntity eeILogin;

    @JsonIgnore
    public EELoginRequest(EeILoginEntity eeILogin) {
        this.eeILogin = eeILogin;
    }
}
