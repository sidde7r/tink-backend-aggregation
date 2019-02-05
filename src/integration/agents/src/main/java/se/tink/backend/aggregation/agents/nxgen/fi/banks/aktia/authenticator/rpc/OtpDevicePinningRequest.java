package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants;

public class OtpDevicePinningRequest {
    private final boolean usePin;
    private final String otpCode;
    private final String deviceName;
    private final boolean useTouch;

    private OtpDevicePinningRequest(String otpCode) {
        this(true, otpCode, AktiaConstants.Session.DEVICE_NAME, false);
    }

    private OtpDevicePinningRequest(boolean usePin, String otpCode, String deviceName, boolean useTouch) {
        this.usePin = usePin;
        this.otpCode = otpCode;
        this.deviceName = deviceName;
        this.useTouch = useTouch;
    }

    public static OtpDevicePinningRequest createFromOtpCode(String otpCode) {
        return new OtpDevicePinningRequest(otpCode);
    }

    public String getOtpCode() {
        return otpCode;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public boolean isUsePin() {
        return usePin;
    }

    public boolean isUseTouch() {
        return useTouch;
    }
}
