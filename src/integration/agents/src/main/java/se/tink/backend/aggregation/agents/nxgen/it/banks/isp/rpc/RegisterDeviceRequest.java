package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.entity.RegisterDeviceRequestPayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDeviceRequest {

    @JsonProperty private RegisterDeviceRequestPayload payload;

    public RegisterDeviceRequest(RegisterDeviceRequestPayload payload) {
        this.payload = payload;
    }
}
