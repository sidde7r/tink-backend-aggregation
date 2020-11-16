package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AuthenticationsResponse {
    String bankidIntegrationUrl;
    String sessionId;
    String status;
}
