package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class FidorConfiguration implements ClientConfiguration {

    private String clientId;
    private String clientSecret;
    private String clientKeyPath;
    private String clientCertificatePath;
    private String clientKeyStorePath;
    private String clinetKeyStorePassword;
    private String baseUrl;

    public String getClientKeyStorePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientKeyStorePath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client key store path"));

        return clientKeyStorePath;
    }

    public String getClinetKeyStorePassword() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clinetKeyStorePassword),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client key store password"));
        return clinetKeyStorePassword;
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

    public String getClientKeyPath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientKeyPath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client key path"));

        return clientKeyPath;
    }

    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "baseUrl is missing"));

        return baseUrl;
    }

    public String getClientCertificatePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientCertificatePath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client certificate path"));

        return clientCertificatePath;
    }
}
