package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class SBABConfiguration implements ClientConfiguration {

    private String baseUrl;
    private String clientId;
    private String redirectUrl;
    private String clientCertificatePath;

    public String getClientCertificatePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientCertificatePath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client certificate path"));

        return clientCertificatePath;
    }

    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base URL"));

        return baseUrl;
    }

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));

        return redirectUrl;
    }
}
