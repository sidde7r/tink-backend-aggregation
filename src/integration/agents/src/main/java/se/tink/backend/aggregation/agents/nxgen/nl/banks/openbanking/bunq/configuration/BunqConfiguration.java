package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqBaseConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class BunqConfiguration extends BunqBaseConfiguration implements ClientConfiguration {

    @JsonProperty
    private String redirectUrl;

    @JsonProperty
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}
