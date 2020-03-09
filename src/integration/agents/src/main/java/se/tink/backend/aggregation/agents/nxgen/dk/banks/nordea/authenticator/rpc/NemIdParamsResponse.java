package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.NemidParamsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class NemIdParamsResponse {

    private NemidParamsEntity nemidParams;
    private String sessionId;
    private String status;

    public NemidParamsEntity getNemidParams() {
        return nemidParams;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getStatus() {
        return status;
    }
}
