package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.Environment;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class SbabConfiguration implements ClientConfiguration {

    private Environment environment;
    private String basicAuthUsername;
    private String basicAuthPassword;
    private String clientId;
    private String accessToken;
    private String redirectUri;

    public Environment getEnvironment() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(environment.toString()),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Environment"));

        return environment;
    }

    public String getBasicAuthUsername() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(basicAuthUsername),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Basic Auth Username"));

        return basicAuthUsername;
    }

    public String getBasicAuthPassword() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(basicAuthPassword),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Basic Auth Password"));

        return basicAuthPassword;
    }

    public String getAccessToken() {
        if (environment == Environment.SANDBOX) {
            Preconditions.checkNotNull(
                    Strings.emptyToNull(sandboxAccessToken),
                    ErrorMessages.MISSING_SANDBOX_ACCESS_TOKEN);
        }

        return sandboxAccessToken;
    }

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getRedirectUri() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUri),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));

        return redirectUri;
    }
}
