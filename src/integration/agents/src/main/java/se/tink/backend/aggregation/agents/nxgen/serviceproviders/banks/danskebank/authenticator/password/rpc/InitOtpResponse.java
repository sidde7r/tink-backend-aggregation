package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitOtpResponse extends AbstractResponse {
    // @JsonProperty("StatusCode")
    // `StatusCode` is null - cannot define it!
    @JsonProperty("ReasonCode")
    private int reasonCode;

    @JsonProperty("DeviceType")
    private String deviceType;

    @JsonProperty("DeviceSerialNo")
    private String deviceSerialNo;

    @JsonProperty("DeviceCodesLeft")
    private int deviceCodesLeft;

    @JsonProperty("MultipleDevices")
    private boolean multipleDevices;

    @JsonProperty("OTPChallenge")
    private String otpChallenge;

    public int getReasonCode() {
        return reasonCode;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceSerialNo() {
        return deviceSerialNo;
    }

    public int getDeviceCodesLeft() {
        return deviceCodesLeft;
    }

    public boolean isMultipleDevices() {
        return multipleDevices;
    }

    public String getOtpChallenge() {
        return otpChallenge;
    }
}
