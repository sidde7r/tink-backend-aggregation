package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities.ProxyRequestHeaders;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.ProxyRequestMessage;

public class ProxyOauthTokenRevokeRequest extends ProxyRequestMessage<String> {

    public ProxyOauthTokenRevokeRequest(String content) {
        super(
                "/oauth/token/revoke",
                "POST",
                ProxyRequestHeaders.builder().accept("application/json").build(),
                content,
                "application/x-www-form-urlencoded");
    }
}
