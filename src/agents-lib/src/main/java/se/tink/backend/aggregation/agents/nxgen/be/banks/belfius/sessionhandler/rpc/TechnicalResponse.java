package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.sessionhandler.rpc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.ResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer.TechnicalResponseDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonDeserialize(using = TechnicalResponseDeserializer.class)
public class TechnicalResponse extends ResponseEntity {

    private final String type;
    private Long remainingTimeBeforeSessionTimeout;

    public TechnicalResponse(String type, Long remainingTimeBeforeSessionTimeout) {
        this.type = type;
        this.remainingTimeBeforeSessionTimeout = remainingTimeBeforeSessionTimeout;
    }

    public void checkSessionExpired() {
        if (BelfiusConstants.Response.TYPE_HEARTBEAT.equalsIgnoreCase(type)
                && remainingTimeBeforeSessionTimeout == 0) {
            throw new IllegalStateException("Session expired");
        }
    }
}
