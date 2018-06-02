package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc.OmaspBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDeviceResponse extends OmaspBaseResponse {
    private String deviceId;

    public String getDeviceId() {
        return deviceId;
    }
}
