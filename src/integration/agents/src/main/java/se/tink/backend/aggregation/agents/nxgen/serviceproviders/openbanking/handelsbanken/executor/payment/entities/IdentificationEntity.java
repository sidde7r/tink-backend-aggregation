package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentificationEntity {
    private String code;
    private String type;

    public IdentificationEntity(String code, String type) {
        this.code = code;
        this.type = type;
    }
}
