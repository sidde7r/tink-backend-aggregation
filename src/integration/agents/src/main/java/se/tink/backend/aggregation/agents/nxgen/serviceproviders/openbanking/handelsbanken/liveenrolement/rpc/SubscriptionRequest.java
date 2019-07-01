package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.entity.App;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SubscriptionRequest {

    @JsonProperty("app")
    private App app;

    @JsonProperty("product")
    private Product product;

    public SubscriptionRequest(App app, Product product) {
        this.app = app;
        this.product = product;
    }

    public App getApp() {
        return app;
    }

    public Product getProduct() {
        return product;
    }
}
