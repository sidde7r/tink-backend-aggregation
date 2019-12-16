package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.entity.ConfirmDeviceRequestPayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConfirmDeviceRequest {

    @JsonProperty private ConfirmDeviceRequestPayload payload;

    public ConfirmDeviceRequest(ConfirmDeviceRequestPayload payload) {
        this.payload = payload;
    }
}
