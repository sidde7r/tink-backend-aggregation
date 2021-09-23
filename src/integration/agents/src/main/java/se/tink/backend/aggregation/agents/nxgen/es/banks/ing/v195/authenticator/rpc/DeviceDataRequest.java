package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import lombok.Value;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Value
public class DeviceDataRequest {
    DeviceData deviceData;

    @JsonObject
    @Value
    public static final class DeviceData {
        String appVersion = "3.4.2";
        String deviceBrand = "iOS";
        String deviceModel = "iPhone 8";
    }

    public DeviceDataRequest() {
        this.deviceData = new DeviceData();
    }
}
