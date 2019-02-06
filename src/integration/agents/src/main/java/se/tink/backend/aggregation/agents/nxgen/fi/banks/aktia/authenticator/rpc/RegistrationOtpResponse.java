package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegistrationOtpResponse {
    private boolean success;
    private String status;
    private String deviceActivationCode;
    // `keyCardInfo` is null - cannot define it!

    public boolean isSuccess() {
        return success;
    }

    public String getStatus() {
        return status;
    }

    public String getDeviceActivationCode() {
        return deviceActivationCode;
    }
}
