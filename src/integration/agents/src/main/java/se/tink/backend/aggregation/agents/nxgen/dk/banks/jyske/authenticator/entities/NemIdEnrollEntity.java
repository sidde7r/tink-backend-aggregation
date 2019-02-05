package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Encryptable;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdEnrollEntity implements Encryptable {
    private String key;
    private String keycardNo;
    private String keyNo;
    private String mobileCode;
    private NemIdSecurityDeviceEntity securityDevice;

    public String getKeyNo() {
        return keyNo;
    }

    public void setKeyNo(String keyNo) {
        this.keyNo = keyNo;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKeycardNo() {
        return keycardNo;
    }

    public void setKeycardNo(String keycardNo) {
        this.keycardNo = keycardNo;
    }

    public String getMobileCode() {
        return mobileCode;
    }

    public void setMobileCode(String mobileCode) {
        this.mobileCode = mobileCode;
    }

    public NemIdSecurityDeviceEntity getSecurityDevice() {
        return securityDevice;
    }

    public void setSecurityDevice(
            NemIdSecurityDeviceEntity securityDevice) {
        this.securityDevice = securityDevice;
    }
}
