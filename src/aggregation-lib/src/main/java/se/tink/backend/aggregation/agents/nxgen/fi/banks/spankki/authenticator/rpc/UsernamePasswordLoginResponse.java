package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.entities.LoginStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UsernamePasswordLoginResponse extends SpankkiResponse {
    private LoginStatusEntity loginStatus;

    public LoginStatusEntity getLoginStatus() {
        return loginStatus;
    }
}
