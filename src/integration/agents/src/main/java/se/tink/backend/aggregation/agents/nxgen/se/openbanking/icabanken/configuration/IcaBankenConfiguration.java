package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class IcaBankenConfiguration implements ClientConfiguration {

    @JsonProperty
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }
}
