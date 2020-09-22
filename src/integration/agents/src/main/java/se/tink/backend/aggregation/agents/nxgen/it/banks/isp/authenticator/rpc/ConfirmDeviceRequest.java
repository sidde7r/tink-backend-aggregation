package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.ConfirmDeviceRequestPayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConfirmDeviceRequest {

    private ConfirmDeviceRequestPayload payload;

    public ConfirmDeviceRequest(ConfirmDeviceRequestPayload payload) {
        this.payload = payload;
    }
}
