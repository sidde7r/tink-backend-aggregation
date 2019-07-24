package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class CbiGlobeConfiguration implements ClientConfiguration {

    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String keystorePath;
    private String keystorePassword;
    private String aspspCode;
    private String aspspProductCode;
    private String psuId;
    private String eidasQwac;

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

    public String getKeystorePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(keystorePath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Keystore Path"));

        return keystorePath;
    }

    public String getKeystorePassword() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(keystorePassword),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Keystore Password"));

        return keystorePassword;
    }

    public String getAspspCode() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(aspspCode),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "ASPSP Code"));

        return aspspCode;
    }

    public String getAspspProductCode() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(aspspProductCode),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "ASPSP Product Code"));

        return aspspProductCode;
    }

    public String getPsuId() {
        Preconditions.checkNotNull(
                psuId, String.format(ErrorMessages.INVALID_CONFIGURATION, "PSU ID"));

        return psuId;
    }

    public String getEidasQwac() {
        Preconditions.checkNotNull(
            eidasQwac, String.format(ErrorMessages.INVALID_CONFIGURATION, "EIDAS QWAC"));

        return eidasQwac;
    }
}
