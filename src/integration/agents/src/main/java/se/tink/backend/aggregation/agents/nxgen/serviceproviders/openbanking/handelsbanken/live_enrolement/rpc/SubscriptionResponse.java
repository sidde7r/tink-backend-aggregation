package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.live_enrolement.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.live_enrolement.entity.Subscription;

@Generated("com.robohorse.robopojogenerator")
public class SubscriptionResponse {

    @JsonProperty("clientId")
    private String clientId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("subscription")
    private Subscription subscription;

    @JsonProperty("oauthRedirectURI")
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

    public Subscription getSubscription() {
        return subscription;
    }

    public String getOauthRedirectURI() {
        return oauthRedirectURI;
    }
}
