package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class UniversoConfiguration implements ClientConfiguration {

    @JsonIgnore
    private static final String INVALID_CONFIGURATION =
            "Invalid Configuration: %s cannot be empty or null";

    @JsonProperty @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;
    @JsonProperty @SensitiveSecret private String apiKey;

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(INVALID_CONFIGURATION, "Client Secret"));

        return clientSecret;
    }

    public String getApiKey() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(apiKey), String.format(INVALID_CONFIGURATION, "API key"));

        return apiKey;
    }
}
