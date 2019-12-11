package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Encryptable;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeycardEnrollEntity implements Encryptable {
    private String key;
    private String keycardNo;
    private String keyNo;
    private String mobileCode;
    private NemIdSecurityDeviceEntity securityDevice;

    public KeycardEnrollEntity(
            String key,
            String keycardNo,
            String keyNo,
            String mobileCode,
            NemIdSecurityDeviceEntity securityDevice) {
        this.key = key;
        this.keycardNo = keycardNo;
        this.keyNo = keyNo;
        this.mobileCode = mobileCode;
        this.securityDevice = securityDevice;
    }

    public String getMobileCode() {
        return mobileCode;
    }

    public String getKey() {
        return key;
    }

    public String getKeycardNo() {
        return keycardNo;
    }

    public String getKeyNo() {
        return keyNo;
    }

    public NemIdSecurityDeviceEntity getSecurityDevice() {
        return securityDevice;
    }
}
