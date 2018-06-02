package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdSecurityDeviceEntity {
    private String deviceNameBD;
    private String deviceNameDanid;

    public String getDeviceNameBD() {
        return deviceNameBD;
    }

    public String getDeviceNameDanid() {
        return deviceNameDanid;
    }
}
