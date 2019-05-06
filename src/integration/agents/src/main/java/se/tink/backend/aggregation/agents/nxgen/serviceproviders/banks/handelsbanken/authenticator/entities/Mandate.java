package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Mandate {

    private String customerName;

    @JsonProperty("customerNmbr")
    private String customerNumber;

    @JsonProperty("agreementNmbr")
    private String agreementNumber;

    public String getCustomerName() {
        return customerName;
    }
}
