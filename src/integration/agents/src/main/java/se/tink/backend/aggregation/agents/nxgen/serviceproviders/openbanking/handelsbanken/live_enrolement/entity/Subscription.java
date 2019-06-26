package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.live_enrolement.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Subscription {

    @JsonProperty("product")
    private Product product;

    @JsonProperty("subscriptionId")
    private String subscriptionId;

    public Product getProduct() {
        return product;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }
}
