package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.CheckPinResponsePayload;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CheckPinResponse extends BaseResponse {

    private CheckPinResponsePayload payload;

    public CheckPinResponsePayload getPayload() {
        return payload;
    }
}
