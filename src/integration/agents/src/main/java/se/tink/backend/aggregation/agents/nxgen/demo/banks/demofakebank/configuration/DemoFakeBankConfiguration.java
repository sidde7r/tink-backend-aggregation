package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class DemoFakeBankConfiguration  implements ClientConfiguration {
    @JsonProperty
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }
}
