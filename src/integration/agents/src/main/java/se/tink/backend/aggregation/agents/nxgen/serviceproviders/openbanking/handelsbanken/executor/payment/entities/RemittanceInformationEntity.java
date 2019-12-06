package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RemittanceInformationEntity {
    private String text;

    public RemittanceInformationEntity(String text) {
        this.text = text;
    }
}
