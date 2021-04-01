package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClientInfo {
    private final String clientId;
    private final String tokenEndpointAuthSigningAlg;
    private final String tokenEndpointAuthMethod;
    private final String clientSecret;

    public ClientInfo(
            String clientId, String tokenEndpointAuthMethod, String tokenEndpointAuthSigningAlg) {
        this(clientId, null, tokenEndpointAuthMethod, tokenEndpointAuthSigningAlg);
    }

    public ClientInfo(
            String clientId,
            String clientSecret,
            String tokenEndpointAuthMethod,
            String tokenEndpointAuthSigningAlg) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
        this.tokenEndpointAuthSigningAlg = tokenEndpointAuthSigningAlg;
    }

    public String getTokenEndpointAuthMethod() {
        return tokenEndpointAuthMethod;
    }

    public String getTokenEndpointAuthSigningAlg() {
        return tokenEndpointAuthSigningAlg;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
