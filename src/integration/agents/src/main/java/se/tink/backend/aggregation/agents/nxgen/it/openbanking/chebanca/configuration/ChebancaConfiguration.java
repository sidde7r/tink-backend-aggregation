package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class ChebancaConfiguration implements ClientConfiguration {

    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String certificateId;
    private String applicationId;
    private String baseUrl;

    public ChebancaConfiguration() {}

    public ChebancaConfiguration(
            String clientId,
            String clientSecret,
            String redirectUrl,
            String certificateId,
            String applicationId,
            String baseUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
        this.certificateId = certificateId;
        this.applicationId = applicationId;
        this.baseUrl = baseUrl;
    }

    public String getApplicationId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(applicationId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Application ID"));

        return applicationId;
    }

    public String getCertificateId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(certificateId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Certificate ID"));

        return certificateId;
    }

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
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
}
