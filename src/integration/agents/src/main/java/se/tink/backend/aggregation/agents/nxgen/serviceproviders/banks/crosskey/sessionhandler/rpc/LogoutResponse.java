package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.sessionhandler.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.rpc.CrossKeyResponse;

public class LogoutResponse extends CrossKeyResponse {
    public boolean sessionExpired() {
        return isFailure() && hasAnyErrors("SESSION_EXPIRED");
    }
}
