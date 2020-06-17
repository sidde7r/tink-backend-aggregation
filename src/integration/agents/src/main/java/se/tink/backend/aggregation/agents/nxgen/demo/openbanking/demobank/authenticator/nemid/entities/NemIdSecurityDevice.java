package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdSecurityDevice {
    private String deviceNameBD;
    private String deviceNameDanid;

    public NemIdSecurityDevice setDeviceNameBD(String deviceNameBD) {
        this.deviceNameBD = deviceNameBD;
        return this;
    }

    public NemIdSecurityDevice setDeviceNameDanid(String deviceNameDanid) {
        this.deviceNameDanid = deviceNameDanid;
        return this;
    }
}
