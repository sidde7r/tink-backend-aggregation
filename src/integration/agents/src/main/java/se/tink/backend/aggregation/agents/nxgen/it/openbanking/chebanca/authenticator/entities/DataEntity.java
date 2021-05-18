package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class DataEntity {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("rt_expires_in")
    private long rtExpiresIn;

    private String scope;

    @JsonProperty("token_type")
    private String tokenType;
}
