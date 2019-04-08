package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustomerPropertiesEntity {
    @JsonProperty("mandate")
    private MandateEntity mandate;

    public MandateEntity getMandate() {
        return mandate;
    }
}
