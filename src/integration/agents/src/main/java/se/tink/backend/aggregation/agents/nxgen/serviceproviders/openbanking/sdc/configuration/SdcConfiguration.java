package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class SdcConfiguration implements ClientConfiguration {

    @Secret @ClientIdConfiguration private String clientId;
    @AgentConfigParam private String redirectUrl;
    @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;
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

    public String getOcpApimSubscriptionKey() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(ocpApimSubscriptionKey),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Ocp Api Subscription Key"));

        return ocpApimSubscriptionKey;
    }
}
