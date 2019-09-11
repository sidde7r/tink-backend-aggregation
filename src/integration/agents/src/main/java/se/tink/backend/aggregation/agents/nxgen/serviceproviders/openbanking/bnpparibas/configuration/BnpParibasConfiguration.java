package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class BnpParibasConfiguration implements ClientConfiguration {

    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String eidasQwac;
    private String keyId;
    private String authorizeUrl;
    private String tokenUrl;
    private String baseUrl;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getEidasQwac() {
        return eidasQwac;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public String getAuthorizeUrl() {
        return authorizeUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
