package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SubscriptionProductEntity {

    private String name;

    public SubscriptionProductEntity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
