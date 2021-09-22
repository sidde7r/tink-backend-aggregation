package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DeviceDataRequest {
    private DeviceData deviceData = new DeviceData();

    @JsonObject
    public static class DeviceData {
        private String appVersion = "3.4.2";
        private String deviceBrand = "iOS";
        private String deviceModel = "iPhone 8";
    }
}
