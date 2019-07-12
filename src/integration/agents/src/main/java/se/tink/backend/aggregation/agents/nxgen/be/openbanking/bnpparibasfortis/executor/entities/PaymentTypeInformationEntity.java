package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentTypeInformationEntity {
    private String serviceLevel;

    public PaymentTypeInformationEntity(String serviceLevel) {
        this.serviceLevel = serviceLevel;
    }

    public PaymentTypeInformationEntity() {}

    public String getServiceLevel() {
        return serviceLevel;
    }
}
