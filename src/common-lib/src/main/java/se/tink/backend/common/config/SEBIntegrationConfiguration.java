package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SEBIntegrationConfiguration {
    @JsonProperty
    private SEBMortgageIntegrationConfiguration mortgage;

    public SEBMortgageIntegrationConfiguration getMortgage() {
        return mortgage;
    }
}
