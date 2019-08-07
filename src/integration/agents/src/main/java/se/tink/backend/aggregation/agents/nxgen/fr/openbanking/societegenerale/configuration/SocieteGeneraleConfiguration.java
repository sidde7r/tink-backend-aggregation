package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class SocieteGeneraleConfiguration implements ClientConfiguration {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String eidasQwac;
    private String eidasOrgNmr;
    private String keyId;

    public String getEidasOrgNmr() {
        return eidasOrgNmr;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getEidasQwac() {
        return eidasQwac;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getClientId() {
        return clientId;
    }
}
