package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PutSessionRequest {

    private List<Integer> pinPositions;
    private String processId;

    private PutSessionRequest(List<Integer> pinPositions, String processId) {
        this.pinPositions = pinPositions;
        this.processId = processId;
    }

    public static PutSessionRequest create(List<Integer> pinPositions, String processId) {
        return new PutSessionRequest(pinPositions, processId);
    }

    public List<Integer> getPinPositions() {
        return pinPositions;
    }
}
