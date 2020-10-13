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
}
