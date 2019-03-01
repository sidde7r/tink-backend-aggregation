package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EuOLogoutEntity;

public class LogoutResponse {
    @JsonProperty("EE_O_Logout")
    private EuOLogoutEntity euOLogout;

    public EuOLogoutEntity getEuOLogout() {
        return euOLogout;
    }
}
