package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngCryptoUtils;

public class CryptoInitValues {
    private String deviceId;
    private byte[] sessionKey;
    private byte[] sessionKeyAuth;

    public CryptoInitValues() {
        this.deviceId = IngCryptoUtils.generateDeviceIdHexString();
        this.sessionKey = IngCryptoUtils.getRandomBytes(16);
        this.sessionKeyAuth = IngCryptoUtils.getRandomBytes(16);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }

    public byte[] getSessionKeyAuth() {
        return sessionKeyAuth;
    }
}
