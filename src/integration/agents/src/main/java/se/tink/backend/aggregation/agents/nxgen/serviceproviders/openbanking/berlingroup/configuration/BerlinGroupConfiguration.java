package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class BerlinGroupConfiguration implements ClientConfiguration {
    private String baseUrl;
    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String psuIpAddress;
    private String eidasQwac;

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Secret"));

        return clientSecret;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));

        return redirectUrl;
    }

    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base URL"));

        return baseUrl;
    }

    public String getPsuIpAddress() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psuIpAddress),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "PSU IP Address"));

        return psuIpAddress;
    }

    public String getEidasQwac() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(eidasQwac),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Eidas Qwac"));

        return eidasQwac;
    }
}
