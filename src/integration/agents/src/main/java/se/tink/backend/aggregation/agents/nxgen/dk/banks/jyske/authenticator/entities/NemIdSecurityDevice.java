package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities;

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
