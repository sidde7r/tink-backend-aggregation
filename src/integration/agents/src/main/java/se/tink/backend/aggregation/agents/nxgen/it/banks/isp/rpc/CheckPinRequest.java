package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.entity.CheckPinRequestPayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CheckPinRequest {

    @JsonProperty private CheckPinRequestPayload payload;

    public CheckPinRequest(CheckPinRequestPayload payload) {
        this.payload = payload;
    }
}
