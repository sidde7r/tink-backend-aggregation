package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class CmcicConfiguration implements ClientConfiguration {

    @Secret private String baseUrl;
    @Secret private String basePath;
    @Secret private String authBaseUrl;
    @Secret private String clientId;
    @SensitiveSecret private String keyId;

    public String getKeyId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(keyId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Key ID"));

        return keyId;
    }

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getBasePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(basePath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base Path"));

        return basePath;
    }

    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base URL"));

        return baseUrl;
    }

    public String getAuthBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(authBaseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Auth Base URL"));

        return authBaseUrl;
    }
}
