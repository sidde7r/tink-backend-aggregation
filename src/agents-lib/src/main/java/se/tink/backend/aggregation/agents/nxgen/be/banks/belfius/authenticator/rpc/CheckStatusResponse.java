package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.ExecuteMethodResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CheckStatusResponse extends BelfiusResponse {

    public boolean isDeviceRegistered() {
        String deviceRegistered = (String) filter(ExecuteMethodResponse.class)
                .findFirst().orElseThrow(() -> new IllegalStateException("Error in checkStatus request"))
                .getOutputs().get("DeviceRegistered");

        return deviceRegistered != null && "Y".equalsIgnoreCase(deviceRegistered) ? true : false;
    }
}
