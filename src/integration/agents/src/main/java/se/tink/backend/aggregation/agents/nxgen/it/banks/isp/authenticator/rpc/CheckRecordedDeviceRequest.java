package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.CheckRecordedDeviceRequestPayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class CheckRecordedDeviceRequest {
    private CheckRecordedDeviceRequestPayload payload;
}
