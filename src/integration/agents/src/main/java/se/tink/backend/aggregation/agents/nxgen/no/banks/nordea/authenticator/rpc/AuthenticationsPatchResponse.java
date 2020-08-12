package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class AuthenticationsPatchResponse {
    private String code;
    private String sessionId;
    private String status;
}
