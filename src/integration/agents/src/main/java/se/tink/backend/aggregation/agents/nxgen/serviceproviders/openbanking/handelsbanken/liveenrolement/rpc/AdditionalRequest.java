package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.entity.SubscriptionProductEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AdditionalRequest {

    private SubscriptionProductEntity product;

    private String clientId;

    public SubscriptionProductEntity getProduct() {
        return product;
    }

    public String getClientId() {
        return clientId;
    }

    public AdditionalRequest(String clientId, SubscriptionProductEntity product) {
        this.clientId = clientId;
        this.product = product;
    }
}
