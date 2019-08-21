package se.tink.backend.aggregation.register.nl.bunq.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.register.nl.bunq.entities.CallbackUrlEntity;

public class GetCallbackResponse {

    @JsonProperty("OauthCallbackUrl")
    private CallbackUrlEntity callbackUrlEntity;

    public CallbackUrlEntity getCallbackUrlEntity() {
        return callbackUrlEntity;
    }
}
