package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Creditor {
    private String name;

    public Creditor(String name) {
        this.name = name;
    }
}
