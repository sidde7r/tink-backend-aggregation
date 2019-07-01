package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.entity.AppEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.entity.SubscriptionProductEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SubscriptionRequest {

    private AppEntity appEntity;

    private SubscriptionProductEntity subscriptionProductEntity;

    public SubscriptionRequest(AppEntity app, SubscriptionProductEntity product) {
        this.appEntity = app;
        this.subscriptionProductEntity = product;
    }

    public AppEntity getAppEntity() {
        return appEntity;
    }

    public SubscriptionProductEntity getSubscriptionProductEntity() {
        return subscriptionProductEntity;
    }
}
