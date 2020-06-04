package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class AbancaConfiguration implements ClientConfiguration {

    @Secret private String clientId;
    @SensitiveSecret private String apiKey;

    public String getApiKey() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(apiKey),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "API Key"));

        return apiKey;
    }

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }
}
