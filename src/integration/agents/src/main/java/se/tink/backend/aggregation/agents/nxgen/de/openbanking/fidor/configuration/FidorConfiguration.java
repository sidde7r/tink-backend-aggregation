package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class FidorConfiguration implements ClientConfiguration {

    @Secret @ClientIdConfiguration private String clientId;
    @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;
    @Secret private String clientKeyPath;
    @Secret private String clientCertificatePath;
    @Secret private String clientKeyStorePath;
    @SensitiveSecret private String clientKeyStorePassword;
    @Secret private String baseUrl;
    @AgentConfigParam private String redirectUrl;

    private String certificateId;

    public String getClientKeyStorePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientKeyStorePath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client key store path"));

        return clientKeyStorePath;
    }

    public String getClientKeyStorePassword() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientKeyStorePassword),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client key store password"));
        return clientKeyStorePassword;
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

    public String getCertificateId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(certificateId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Certificate ID"));
        return certificateId;
    }

    public String getRedirectUri() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "RedirectUri"));
        return redirectUrl;
    }
}
