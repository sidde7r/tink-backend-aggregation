package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class DecoupledAuthenticationResponse {
    private String sessionId;
    private String autoStartToken;
    private String status;
    private int verifyAfter;
    private String code;
}
