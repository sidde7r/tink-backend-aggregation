package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.configuration;

import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class BnpParibasFortisConfiguration implements ClientConfiguration {

    @Secret private String clientId;
    @SensitiveSecret private String clientSecret;
    @AgentConfigParam private String redirectUrl;
    @Secret private String authBaseUrl;
    @Secret private String apiBaseUrl;
    @Secret private String organisationId;
    @Secret private String openbankStetVersion;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getAuthBaseUrl() {
        return authBaseUrl;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public String getOrganisationId() {
        return organisationId;
    }

    public String getOpenbankStetVersion() {
        return openbankStetVersion;
    }
}
