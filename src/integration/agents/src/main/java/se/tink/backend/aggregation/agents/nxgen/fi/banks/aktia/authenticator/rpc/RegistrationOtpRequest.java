package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegistrationOtpRequest {
    private String clientSoftwareVersion;
    private String otp;
    private String deviceName;
    private String clientSoftwareName;
    private String deviceBrand;
    private String deviceOs;
    private String deviceModel;

    public RegistrationOtpRequest(String otp) {
        this.otp = otp;
        this.clientSoftwareName = AktiaConstants.Avain.SOFTWARE_NAME;
        this.clientSoftwareVersion = AktiaConstants.Avain.SOFTWARE_VERSION;

        // Note: The names are a bit misleading. This is an example of this model:
        // "deviceName": "iPhone 6 Plus",
        // "deviceBrand": "Apple",
        // "deviceOs": "IOS",
        // "deviceModel": "10.3.1"

        this.deviceName = AktiaConstants.DEVICE_PROFILE.getPhoneModel();
        this.deviceBrand = AktiaConstants.DEVICE_PROFILE.getMake();
        this.deviceOs = AktiaConstants.DEVICE_PROFILE.getOs().toUpperCase();
        this.deviceModel = AktiaConstants.DEVICE_PROFILE.getOsVersion();
    }
}
