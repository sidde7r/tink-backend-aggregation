package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateSessionRequest {

    @JsonProperty("pushRegistrationToken")
    private String pushRegistrationToken;

    @JsonProperty("applicationInstanceID")
    private String applicationInstanceID;

    @JsonIgnore
    public CreateSessionRequest(String pushRegistrationToken, String applicationInstanceID) {
        this.pushRegistrationToken = pushRegistrationToken;
        this.applicationInstanceID = applicationInstanceID;
    }
}
