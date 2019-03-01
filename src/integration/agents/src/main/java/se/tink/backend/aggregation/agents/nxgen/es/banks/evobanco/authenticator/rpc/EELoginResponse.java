package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EeOLoginEntity;

public class EELoginResponse {
    @JsonProperty("EE_O_Login")
    private EeOLoginEntity eeOLogin;

    public EeOLoginEntity getEeOLogin() {
        return eeOLogin;
    }
}
