package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorCodes;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FaultResponse {
    private String faultcode;

    @JsonIgnore
    public boolean isServerFault() {
        return ErrorCodes.SERVER.equalsIgnoreCase(faultcode);
    }
}
