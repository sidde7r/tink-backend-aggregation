package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.rpc.AlandsBankenResponse;

public class AddDeviceResponse extends AlandsBankenResponse {

    private String deviceId;
    private String token;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
