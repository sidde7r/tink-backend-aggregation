package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.mobileapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
public class LoginDeviceRequest {
    private String deviceOS;
    private String appVersion;
    private String deviceModel;

    @JsonProperty("deviceId")
    private String internalDeviceId;

    private String deviceName;
    private String deviceManufacturer;
}
