package se.tink.backend.aggregation.agents.nxgen.fi.banks.danskebank.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchHouseholdFIResponse {
    @JsonProperty private String customerInternalId;

    @JsonProperty private String customerExternalId;

    @JsonProperty private String customerName;

    public String getCustomerExternalId() {
        return customerExternalId;
    }

    public String getCustomerName() {
        return customerName;
    }
}
