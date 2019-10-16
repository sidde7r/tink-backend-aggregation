package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc.SdcConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class SdcConfiguration implements ClientConfiguration {

    @Secret private String clientId;
    @SensitiveSecret private String clientSecret;
    @Secret private String redirectUrl;
    @Secret private String clientKeyStorePath;
    @SensitiveSecret private String clientKeyStorePassword;
    @SensitiveSecret private String ocpApimSubscriptionKey;

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

    public String getOcpApimSubscriptionKey() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(ocpApimSubscriptionKey),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Ocp Api Subscription Key"));

        return ocpApimSubscriptionKey;
    }
}
