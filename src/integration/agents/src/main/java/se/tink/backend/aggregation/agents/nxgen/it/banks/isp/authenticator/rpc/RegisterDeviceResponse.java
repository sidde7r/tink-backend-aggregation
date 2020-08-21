package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.RegisterDeviceResponsePayload;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDeviceResponse extends BaseResponse {

    @JsonProperty private RegisterDeviceResponsePayload payload;

    public RegisterDeviceResponsePayload getPayload() {
        return payload;
    }
}
