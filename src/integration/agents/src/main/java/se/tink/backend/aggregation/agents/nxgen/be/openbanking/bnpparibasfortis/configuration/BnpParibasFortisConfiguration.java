package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class BnpParibasFortisConfiguration implements ClientConfiguration {

    @JsonProperty private String clientId;
    @JsonProperty private String clientSecret;
    @JsonProperty private String redirectUri;
    @JsonProperty private String authBaseUrl;
    @JsonProperty private String apiBaseUrl;
    @JsonProperty private String organisationId;
    @JsonProperty private String openbankStetVersion;

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(
                        BnpParibasFortisConstants.ErrorMessages
                                .INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL,
                        "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(
                        BnpParibasFortisConstants.ErrorMessages
                                .INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL,
                        "Client secret"));

        return clientSecret;
    }

    public String getRedirectUri() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUri),
                String.format(
                        BnpParibasFortisConstants.ErrorMessages
                                .INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL,
                        "Redirect URI"));

        return redirectUri;
    }

    public String getAuthBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(authBaseUrl),
                String.format(
                        BnpParibasFortisConstants.ErrorMessages
                                .INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL,
                        "Auth Base URL"));

        return authBaseUrl;
    }

    public String getApiBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(apiBaseUrl),
                String.format(
                        BnpParibasFortisConstants.ErrorMessages
                                .INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL,
                        "API Base URL"));

        return apiBaseUrl;
    }

    public String getOrganisationId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(organisationId),
                String.format(
                        BnpParibasFortisConstants.ErrorMessages
                                .INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL,
                        "Organisation ID"));

        return organisationId;
    }

    public String getOpenbankStetVersion() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(openbankStetVersion),
                String.format(
                        BnpParibasFortisConstants.ErrorMessages
                                .INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL,
                        "Openbank Stet Version"));

        return openbankStetVersion;
    }
}
