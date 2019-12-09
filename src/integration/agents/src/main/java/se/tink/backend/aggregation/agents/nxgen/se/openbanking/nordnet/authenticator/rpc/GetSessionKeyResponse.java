package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.authenticator.entities.PrivateFeedEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.authenticator.entities.PublicFeedEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetSessionKeyResponse {
    @JsonProperty("session_key")
    private String sessionKey;

    @JsonProperty("expires_in")
    private int expiresIn;

    private String environment;

    private String country;

    @JsonProperty("private_feed")
    private PrivateFeedEntity privateFeed;

    @JsonProperty("public_feed")
    private PublicFeedEntity publicFeed;

    public String getSessionKey() {
        return sessionKey;
    }
}
