package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration;

import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class SocieteGeneraleConfiguration implements ClientConfiguration {

    @Secret private String clientId;
    @SensitiveSecret private String clientSecret;
    @AgentConfigParam private String redirectUrl;
    @Secret private String eidasQwac;
    @Secret private String eidasOrgNmr;
    @Secret private String keyId;

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

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getClientId() {
        return clientId;
    }
}
