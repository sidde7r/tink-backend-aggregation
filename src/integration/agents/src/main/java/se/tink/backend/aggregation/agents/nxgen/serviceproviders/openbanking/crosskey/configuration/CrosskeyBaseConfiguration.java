package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.Environment;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class CrosskeyBaseConfiguration implements ClientConfiguration {

    @JsonProperty private String clientId;
    @JsonProperty private String clientSecret;
    @JsonProperty private String redirectUrl;
    @JsonProperty private String clientKeyStorePath;
    @JsonProperty private String clientKeyStorePassword;
    @JsonProperty private String clientSigningKeyPath;
    @JsonProperty private String clientSigningCertificatePath;
    @JsonProperty private String xFapiFinancialId;
    @JsonProperty private String eidasProxyBaseUrl;
    @JsonProperty private String signingKeySerial;
    @JsonProperty private String environment;
    @JsonProperty private String certificateId;
    @JsonProperty private String financialInstitutionId;
    @JsonProperty private String appId;
    @JsonProperty private String clusterId;

    public void setxFapiFinancialId(String xFapiFinancialId) {
        this.xFapiFinancialId = xFapiFinancialId;
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

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));

        return redirectUrl;
    }

    public String getXFapiFinancialId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(xFapiFinancialId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "x-fapi-financial-id"));

        return xFapiFinancialId;
    }

    public String getEidasProxyBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(eidasProxyBaseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Eidas proxy base URL"));

        return eidasProxyBaseUrl;
    }

    public String getClientKeyStorePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientKeyStorePath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Key Store Path"));

        return clientKeyStorePath;
    }

    public String getClientSigningKeyPath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningKeyPath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Signing Key Path"));

        return clientSigningKeyPath;
    }

    public String getClientSigningCertificatePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningCertificatePath),
                String.format(
                        ErrorMessages.INVALID_CONFIGURATION, "Client Signing Certificate Path"));

        return clientSigningCertificatePath;
    }

    public String getClientKeyStorePassword() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientKeyStorePassword),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Key Store Password"));

        return clientKeyStorePassword;
    }

    public String getSigningKeySerial() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(xFapiFinancialId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "signing key serial"));

        return signingKeySerial;
    }

    @JsonIgnore
    public Environment getEnvironment() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(environment),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "environment"));
        return Environment.fromString(environment);
    }

    public String getCertificateId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(certificateId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "certificateId"));
        return certificateId;
    }

    public String getFinancialInstitutionId() {
        return financialInstitutionId;
    }

    public void setFinancialInstitutionId(String financialInstitutionId) {
        this.financialInstitutionId = financialInstitutionId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }
}
