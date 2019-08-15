package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;
import se.tink.backend.aggregation.configuration.Environment;

@JsonObject
public class TargobankConfiguration implements ClientConfiguration {

    private String redirectUrl;
    private String apiKey;
    private String psuIpAddress;
    private String clientKeyStorePath;
    private String clientKeyStorePassword;
    private String scaPassword;
    private String environment;

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));

        return redirectUrl;
    }

    public String getApiKey() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(apiKey),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "API Key"));

        return apiKey;
    }

    public String getPsuIpAddress() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psuIpAddress),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "PSU IP Address"));

        return psuIpAddress;
    }

    public String getClientKeyStorePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientKeyStorePath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Key Store Path"));

        return clientKeyStorePath;
    }

    public String getClientKeyStorePassword() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientKeyStorePassword),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Key Store Password"));

        return clientKeyStorePassword;
    }

    public String getScaPassword() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(scaPassword),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Sca Password"));

        return scaPassword;
    }

    public Environment getEnvironment() {
        return Environment.fromString(environment);
    }
}
