package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SubscriptionEntity {

    private ProductEntity productEntity;

    private String subscriptionId;

    public ProductEntity getProductEntity() {
        return productEntity;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }
}
