package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ClientRegistrationResponse {
    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_name")
    private String clientName;

    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("registration_access_token")
    private String registrationAccessToken;

    @JsonProperty("registration_client_uri")
    private String registrationClientUri;

    private String scope;
}
