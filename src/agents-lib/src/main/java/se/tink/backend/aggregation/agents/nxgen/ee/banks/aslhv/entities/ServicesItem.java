package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ServicesItem {

    @JsonProperty("terms_of_service")
    private String termsOfService;

    @JsonProperty("name")
    private String name;

    @JsonProperty("enabled")
    private boolean enabled;

    public String getTermsOfService() {
        return termsOfService;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
