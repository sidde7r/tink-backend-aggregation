package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class SkandiaConfiguration implements ClientConfiguration {
    private String redirectUrl;
    private String clientId;
    private String clientSecret;
    private String xClientCertificate;

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));

        return redirectUrl;
    }

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

    public String getxClientCertificate() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(xClientCertificate),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "X Client Certificate"));

        return xClientCertificate;
    }
}
