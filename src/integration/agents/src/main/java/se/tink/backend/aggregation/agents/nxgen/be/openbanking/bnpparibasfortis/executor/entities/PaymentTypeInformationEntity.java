package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentTypeInformationEntity {
    private String serviceLevel;

    @JsonCreator
    public PaymentTypeInformationEntity(@JsonProperty("serviceLevel") String serviceLevel) {
        this.serviceLevel = serviceLevel;
    }

    public PaymentTypeInformationEntity() {}

    String getServiceLevel() {
        return serviceLevel;
    }
}
