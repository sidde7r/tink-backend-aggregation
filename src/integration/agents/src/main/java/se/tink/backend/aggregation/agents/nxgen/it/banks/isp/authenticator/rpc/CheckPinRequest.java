package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.CheckPinRequestPayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CheckPinRequest {

    @JsonProperty private CheckPinRequestPayload payload;

    public CheckPinRequest(CheckPinRequestPayload payload) {
        this.payload = payload;
    }
}
