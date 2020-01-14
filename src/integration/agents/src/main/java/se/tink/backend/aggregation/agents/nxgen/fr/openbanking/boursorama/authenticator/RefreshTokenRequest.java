package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RefreshTokenRequest {

    @JsonProperty("refresh_token")
    private String refreshToken;

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
