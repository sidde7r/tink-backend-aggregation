package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

public class CrosskeyBaseConfiguration implements ClientConfiguration {

    @JsonIgnore
    private static final Logger logger = LoggerFactory.getLogger(CrosskeyBaseConfiguration.class);

    @JsonProperty private String clientId;
    @JsonProperty private String clientSecret;
    @JsonProperty private String redirectUrl;
    @JsonProperty private String baseAuthUrl;
    @JsonProperty private String baseAPIUrl;
    @JsonProperty private String clientKeyStorePath;
    @JsonProperty private String clientKeyStorePassword;
    @JsonProperty private String clientSigningKeyPath;
    @JsonProperty private String clientSigningCertificatePath;
    @JsonProperty private String xFapiFinancialId;

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL, "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(
                        ErrorMessages.INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL, "Client Secret"));

        return clientSecret;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(
                        ErrorMessages.INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL, "Redirect URL"));

        return redirectUrl;
    }

    public String getBaseAuthUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseAuthUrl),
                String.format(
                        ErrorMessages.INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL, "Base Auth URL"));

        return baseAuthUrl;
    }

    public String getBaseAPIUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseAPIUrl),
                String.format(
                        ErrorMessages.INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL, "Base API URL"));

        return baseAPIUrl;
    }

    public String getClientKeyStorePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientKeyStorePath),
                String.format(
                        ErrorMessages.INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL,
                        "Client Key Store Path"));

        return clientKeyStorePath;
    }

    public String getClientSigningKeyPath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningKeyPath),
                String.format(
                        ErrorMessages.INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL,
                        "Client Signing Key Path"));

        return clientSigningKeyPath;
    }

    public String getClientSigningCertificatePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningCertificatePath),
                String.format(
                        ErrorMessages.INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL,
                        "Client Signing Certificate Path"));

        return clientSigningCertificatePath;
    }

    public String getClientKeyStorePassword() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientKeyStorePassword),
                String.format(
                        ErrorMessages.INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL,
                        "Client Key Store Password"));

        return clientKeyStorePassword;
    }

    public String getXFapiFinancialId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(xFapiFinancialId),
                String.format(
                        ErrorMessages.INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL,
                        "x-fapi-financial-id"));

        return xFapiFinancialId;
    }
}
