package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class CbiGlobeConfiguration implements ClientConfiguration {

    @Secret private String clientId;
    @SensitiveSecret private String clientSecret;
    @Secret private String redirectUrl;
    @Secret private String keystorePath;
    @SensitiveSecret private String keystorePassword;
    @Secret private String aspspCode;
    @Secret private String aspspProductCode;
    @Secret private String eidasQwac;

    private String environment;

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

    public String getEidasQwac() {
        Preconditions.checkNotNull(
                eidasQwac, String.format(ErrorMessages.INVALID_CONFIGURATION, "EIDAS QWAC"));

        return eidasQwac;
    }

    public String getKeystorePath() {
        Preconditions.checkNotNull(
                keystorePath, String.format(ErrorMessages.INVALID_CONFIGURATION, "Keystore Path"));

        return keystorePath;
    }

    public String getKeystorePassword() {
        Preconditions.checkNotNull(
                keystorePassword,
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Keystore Password"));

        return keystorePassword;
    }

    public Environment getEnvironment() {
        return Environment.fromString(environment);
    }

    public enum Environment {
        SANDBOX("sandbox"),
        PRODUCTION("production");

        private final String value;

        Environment(String value) {
            this.value = value;
        }

        public static Environment fromString(String value) {
            return Environment.valueOf(value.toUpperCase());
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
