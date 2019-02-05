package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class ListOtpResponse extends AbstractResponse {
    @JsonProperty("Devices")
    private List<DeviceEntity> devices;

    public List<DeviceEntity> getDevices() {
        return devices;
    }
}
