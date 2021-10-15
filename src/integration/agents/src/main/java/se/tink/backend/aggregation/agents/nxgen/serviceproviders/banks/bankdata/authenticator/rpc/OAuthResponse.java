package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OAuthResponse {
    private String accessToken;
    private String refreshToken;
    private String scope;
    private String tokenType;
    private int expiresIn;
}
