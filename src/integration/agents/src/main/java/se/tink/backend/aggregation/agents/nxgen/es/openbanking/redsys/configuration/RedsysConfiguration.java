package se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class RedsysConfiguration implements ClientConfiguration {

    private String baseUrl;
    private String clientId;
    private String authClientId;
    private String redirectUrl;
    private String aspsp;

    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base URL"));

        return baseUrl;
    }

    public String getAuthClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(authClientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID for authentication"));

        return authClientId;
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

    public String getAspsp() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(aspsp),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "ASPSP"));

        return aspsp;
    }
}
