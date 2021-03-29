package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClientInfo {

    private String clientId;
    private String clientSecret;
    private String tokenEndpointAuthSigningAlg;
    private String tokenEndpointAuthMethod;

    public String getTokenEndpointAuthMethod() {
        return tokenEndpointAuthMethod;
    }

    public String getTokenEndpointAuthSigningAlg() {
        return tokenEndpointAuthSigningAlg;
    }

    public ClientInfo(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public ClientInfo(
            String clientId, String tokenEndpointAuthMethod, String tokenEndpointAuthSigningAlg) {
        this.clientId = clientId;
        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
        this.tokenEndpointAuthSigningAlg = tokenEndpointAuthSigningAlg;
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

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
