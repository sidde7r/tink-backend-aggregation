package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.RegisterDeviceRequestPayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class RegisterDeviceRequest {

    private RegisterDeviceRequestPayload payload;

    public RegisterDeviceRequest(RegisterDeviceRequestPayload payload) {
        this.payload = payload;
    }
}
