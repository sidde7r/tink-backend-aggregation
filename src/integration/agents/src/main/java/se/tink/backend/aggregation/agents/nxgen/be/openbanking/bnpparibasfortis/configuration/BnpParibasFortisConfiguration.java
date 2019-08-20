package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class BnpParibasFortisConfiguration implements ClientConfiguration {

    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String authBaseUrl;
    private String apiBaseUrl;
    private String organisationId;
    private String openbankStetVersion;

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(
                        BnpParibasFortisConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(
                        BnpParibasFortisConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Client secret"));

        return clientSecret;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(
                        BnpParibasFortisConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Redirect URL"));

        return redirectUrl;
    }

    public String getAuthBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(authBaseUrl),
                String.format(
                        BnpParibasFortisConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Auth Base URL"));

        return authBaseUrl;
    }

    public String getApiBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(apiBaseUrl),
                String.format(
                        BnpParibasFortisConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "API Base URL"));

        return apiBaseUrl;
    }

    public String getOrganisationId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(organisationId),
                String.format(
                        BnpParibasFortisConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Organisation ID"));

        return organisationId;
    }

    public String getOpenbankStetVersion() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(openbankStetVersion),
                String.format(
                        BnpParibasFortisConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Openbank Stet Version"));

        return openbankStetVersion;
    }
}
