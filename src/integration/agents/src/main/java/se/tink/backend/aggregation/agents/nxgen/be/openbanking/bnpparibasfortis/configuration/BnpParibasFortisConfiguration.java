package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class BnpParibasFortisConfiguration implements ClientConfiguration {
    @JsonProperty private String clientId;

    @JsonProperty private String clientSecret;

    @JsonProperty private String redirectUri;

    @JsonProperty private String authBaseUrl;

    @JsonProperty private String organisationId;

    @JsonProperty private String openbankStetVersion;

    @JsonProperty private String apiBaseUrl;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getAuthBaseUrl() {
        return authBaseUrl;
    }

    public String getOrganisationId() {
        return organisationId;
    }

    public String getOpenbankStetVersion() {
        return openbankStetVersion;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }
}
