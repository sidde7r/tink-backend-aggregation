package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PutSessionRequest {

    private List<Integer> pinPositions;

    private PutSessionRequest(List<Integer> pinPositions) {
        this.pinPositions = pinPositions;
    }

    public static PutSessionRequest create(List<Integer> pinPositions) {
        return new PutSessionRequest(pinPositions);
    }

    public List<Integer> getPinPositions() {
        return pinPositions;
    }
}
