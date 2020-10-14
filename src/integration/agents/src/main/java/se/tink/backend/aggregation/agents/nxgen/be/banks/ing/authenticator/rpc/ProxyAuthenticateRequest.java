package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.AuthenticateRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.ProxyRequestMessage;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProxyAuthenticateRequest extends ProxyRequestMessage<AuthenticateRequestEntity> {

    public ProxyAuthenticateRequest(AuthenticateRequestEntity content) {
        super("/ucrmeans/mobile/authenticate", "POST", null, content, "application/json");
    }
}
