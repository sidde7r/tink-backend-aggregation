package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.entity.CheckPinResponsePayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CheckPinResponse extends BaseResponse {

    @JsonProperty private CheckPinResponsePayload payload;

    public CheckPinResponsePayload getPayload() {
        return payload;
    }
}
