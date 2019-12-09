package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SubscriptionEntity {

    @JsonProperty("product")
    private ProductEntity productEntity;

    private String subscriptionId;

    public ProductEntity getProductEntity() {
        return productEntity;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }
}
