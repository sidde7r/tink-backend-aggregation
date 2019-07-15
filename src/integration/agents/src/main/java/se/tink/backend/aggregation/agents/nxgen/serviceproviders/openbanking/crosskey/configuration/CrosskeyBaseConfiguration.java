package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class CrosskeyBaseConfiguration implements ClientConfiguration {

    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String baseAuthUrl;
    private String baseAPIUrl;
    private String clientKeyStorePath;
    private String clientKeyStorePassword;
    private String clientSigningKeyPath;
    private String clientSigningCertificatePath;
    private String xFapiFinancialId;
    private String eidasProxyBaseUrl;

    public void setBaseAPIUrl(String baseAPIUrl) {
        this.baseAPIUrl = baseAPIUrl;
    }

    public void setBaseAuthUrl(String baseAuthUrl) {
        this.baseAuthUrl = baseAuthUrl;
    }

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

    public String getBaseAuthUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseAuthUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base Auth URL"));

        return baseAuthUrl;
    }

    public String getBaseAPIUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseAPIUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base API URL"));

        return baseAPIUrl;
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
}
