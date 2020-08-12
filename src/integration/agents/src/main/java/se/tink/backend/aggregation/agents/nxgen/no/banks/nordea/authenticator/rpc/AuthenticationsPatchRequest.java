package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@AllArgsConstructor
public class AuthenticationsPatchRequest {
    private String bidCode;
}
