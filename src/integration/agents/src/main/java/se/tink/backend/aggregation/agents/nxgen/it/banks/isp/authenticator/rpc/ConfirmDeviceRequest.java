package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.ConfirmDeviceRequestPayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConfirmDeviceRequest {

    @JsonProperty private ConfirmDeviceRequestPayload payload;

    public ConfirmDeviceRequest(ConfirmDeviceRequestPayload payload) {
        this.payload = payload;
    }
}
