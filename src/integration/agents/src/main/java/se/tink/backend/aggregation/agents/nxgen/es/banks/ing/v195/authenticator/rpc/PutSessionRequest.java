package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class PutSessionRequest {

    private List<Integer> pinPositions;
    private String otp;
    private String processId;

    private PutSessionRequest(List<Integer> pinPositions, String processId) {
        this.pinPositions = pinPositions;
        this.processId = processId;
    }

    private PutSessionRequest(String otp, String processId) {
        this.otp = otp;
        this.processId = processId;
    }

    public static PutSessionRequest create(List<Integer> pinPositions, String processId) {
        return new PutSessionRequest(pinPositions, processId);
    }

    public static PutSessionRequest create(String otp, String processId) {
        return new PutSessionRequest(otp, processId);
    }

    public List<Integer> getPinPositions() {
        return pinPositions;
    }
}
