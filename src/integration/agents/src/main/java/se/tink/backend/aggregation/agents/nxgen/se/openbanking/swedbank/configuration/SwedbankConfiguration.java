package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class SwedbankConfiguration implements ClientConfiguration {

    @Secret private String clientId;
    @SensitiveSecret private String clientSecret;
    @Secret private String redirectUrl;
    @Secret private String qSealc;
    @Secret private String keyIdBase64;

    public String getQSealc() {
        Preconditions.checkNotNull(
                com.google.common.base.Strings.emptyToNull(qSealc),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "qSealc"));

        return qSealc;
    }

    public String getKeyIdBase64() {
        Preconditions.checkNotNull(
                com.google.common.base.Strings.emptyToNull(keyIdBase64),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "KeyId"));

        return keyIdBase64;
    }

    public String getClientId() {
        Preconditions.checkNotNull(
                com.google.common.base.Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                com.google.common.base.Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Secret"));

        return clientSecret;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));

        return redirectUrl;
    }
}
