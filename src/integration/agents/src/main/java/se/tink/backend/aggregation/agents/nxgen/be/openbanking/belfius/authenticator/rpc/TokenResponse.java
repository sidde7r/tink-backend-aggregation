package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private String idToken;
    private String logicalId;
    private String tokenType;
    private int expiresIn;
    private String scaToken;
}
