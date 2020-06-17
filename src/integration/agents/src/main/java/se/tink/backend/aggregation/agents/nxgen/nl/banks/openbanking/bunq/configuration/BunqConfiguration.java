package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqConstants;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class BunqConfiguration implements ClientConfiguration {

    @JsonProperty @AgentConfigParam private String redirectUrl;

    @JsonProperty @SensitiveSecret private String psd2ApiKey;

    @JsonProperty @Secret @ClientIdConfiguration private String clientId;

    @JsonProperty @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;

    @JsonProperty @Secret private String psd2InstallationKeyPair;

    @JsonProperty @SensitiveSecret private String psd2ClientAuthToken;

    public String getPsd2InstallationKeyPair() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psd2InstallationKeyPair),
                String.format(
                        BunqConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "PSD2 Installation Key Pair"));

        return psd2InstallationKeyPair;
    }

    public String getPsd2ClientAuthToken() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psd2ClientAuthToken),
                String.format(
                        BunqConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "PSD2 Client Auth Token"));

        return psd2ClientAuthToken;
    }

    public String getPsd2ApiKey() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psd2ApiKey),
                String.format(BunqConstants.ErrorMessages.INVALID_CONFIGURATION, "PSD2 Api Key"));

        return psd2ApiKey;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(BunqConstants.ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));

        return redirectUrl;
    }

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(BunqConstants.ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(BunqConstants.ErrorMessages.INVALID_CONFIGURATION, "Client Secret"));

        return clientSecret;
    }
}
