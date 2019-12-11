package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeycardChallengeEntity {
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

    public NemIdSecurityDeviceEntity getSecurityDevice() {
        return securityDevice;
    }

    public boolean containsValues() {
        return !Strings.isNullOrEmpty(key) && !Strings.isNullOrEmpty(keycardNo);
    }
}
