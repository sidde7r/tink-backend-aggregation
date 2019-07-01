package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.entity.SubscriptionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SubscriptionResponse {

    private String clientId;

    private String name;

    private String description;

    private SubscriptionEntity subscriptionEntity;

    private String oauthRedirectURI;

    public String getClientId() {
        return clientId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public SubscriptionEntity getSubscriptionEntity() {
        return subscriptionEntity;
    }

    public String getOauthRedirectURI() {
        return oauthRedirectURI;
    }
}
