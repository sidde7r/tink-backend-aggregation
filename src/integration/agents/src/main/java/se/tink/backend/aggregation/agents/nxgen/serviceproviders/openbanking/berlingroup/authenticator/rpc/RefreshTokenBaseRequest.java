package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RefreshTokenBaseRequest {

    protected final String grantType;
    protected final String token;
    protected final String clientId;
    protected final String clientSecret;

    public RefreshTokenBaseRequest(
            String grantType, String token, String clientId, String clientSecret) {
        this.grantType = grantType;
        this.token = token;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
}
