package se.tink.backend.aggregation.register.nl.bunq.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.register.nl.bunq.entities.OauthClientEntity;

@JsonObject
public class GetClientIdAndSecretResponse {
    @JsonProperty("OauthClient")
    private OauthClientEntity oauthClient;

    public OauthClientEntity getOauthClient() {
        return oauthClient;
    }
}
