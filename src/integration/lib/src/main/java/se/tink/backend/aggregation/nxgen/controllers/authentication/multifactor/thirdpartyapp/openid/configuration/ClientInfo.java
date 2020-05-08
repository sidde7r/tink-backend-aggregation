package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import java.util.Optional;
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

    public ClientInfo(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
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

    public Optional<String> getTokenEndpointAuthSigningAlg() {
        return Optional.ofNullable(tokenEndpointAuthSigningAlg);
    }
}
