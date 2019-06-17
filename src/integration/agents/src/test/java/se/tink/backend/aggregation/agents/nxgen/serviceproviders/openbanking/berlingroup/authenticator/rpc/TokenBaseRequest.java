package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc;

public abstract class TokenBaseRequest {
    protected String grantType;
    protected String code;
    protected String redirectUri;
    protected String clientId;
    protected String clientSecret;
}
