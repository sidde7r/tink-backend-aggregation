package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class SebConfiguration implements ClientConfiguration {

    @Secret private String clientId;
    @SensitiveSecret private String clientSecret;
    @Secret private String redirectUrl;
    private String baseUrl;
    private String psuIpAddress;

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
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Psu ip address"));

        return psuIpAddress;
    }
}
