package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdChallengeEntity {
    private String key;
    private String keycardNo;
    private String tokenSerial;
    private NemIdSecurityDeviceEntity securityDevice;

    public String getKey() {
        return key;
    }

    public String getKeycardNo() {
        return keycardNo;
    }

    public String getTokenSerial() {
        return tokenSerial;
    }

    public NemIdSecurityDeviceEntity getSecurityDevice() {
        return securityDevice;
    }
}
