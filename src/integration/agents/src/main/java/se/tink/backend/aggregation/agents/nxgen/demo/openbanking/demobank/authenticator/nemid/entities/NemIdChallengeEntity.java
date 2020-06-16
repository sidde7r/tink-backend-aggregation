package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdChallengeEntity {
    private String keycardNo;
    private String tokenSerial;
    private String anyActiveCodeapps;
    private NemIdSecurityDevice securityDevice;

    public String getKeycardNo() {
        return keycardNo;
    }

    public String getTokenSerial() {
        return tokenSerial;
    }

    public String getAnyActiveCodeapps() {
        return anyActiveCodeapps;
    }

    public NemIdSecurityDevice getSecurityDevice() {
        return securityDevice;
    }
}
