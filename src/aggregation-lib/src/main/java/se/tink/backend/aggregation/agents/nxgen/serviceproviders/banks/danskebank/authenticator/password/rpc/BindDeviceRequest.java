package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BindDeviceRequest {
    @JsonProperty("FriendlyName")
    private final String friendlyName;

    private BindDeviceRequest(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public static BindDeviceRequest create(String friendlyName) {
        return new BindDeviceRequest(friendlyName);
    }

    public String getFriendlyName() {
        return friendlyName;
    }
}
