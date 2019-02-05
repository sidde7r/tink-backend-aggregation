package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc;

import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SdcPersistentStorage {
    private static final String PRIVATE_KEY_NAME = "SIGNER_PRIVATE_KEY";
    private static final String PUBLIC_KEY_NAME = "SIGNER_PUBLIC_KEY";
    private static final String DEVICE_ID_NAME = "DEVICE_ID";
    private static final String SIGNED_DEVICE_ID = "SIGNED_DEVICE_ID";

    private final PersistentStorage storage;

    public SdcPersistentStorage(PersistentStorage storage) {
        this.storage = storage;
    }

    public boolean hasSignerKeys() {
        return this.storage.containsKey(PRIVATE_KEY_NAME) && this.storage.containsKey(PUBLIC_KEY_NAME);
    }

    public void putPrivateKey(byte[] key) {
        this.storage.put(PRIVATE_KEY_NAME, Base64.encodeBase64String(key));
    }

    public byte[] getPrivateKey() {
        return Base64.decodeBase64(this.storage.get(PRIVATE_KEY_NAME));
    }

    public void putPublicKey(byte[] key) {
        this.storage.put(PUBLIC_KEY_NAME, Base64.encodeBase64String(key));
    }

    public String getPublicKey() {
        return this.storage.get(PUBLIC_KEY_NAME);
    }

    public void putDeviceId(String deviceId) {
        this.storage.put(DEVICE_ID_NAME, deviceId);
    }

    public String getDeviceId() {
        return this.storage.get(DEVICE_ID_NAME);
    }

    public void putSignedDeviceId(String signedDeviceId) {
        this.storage.put(SIGNED_DEVICE_ID, signedDeviceId);
    }

    public void removeSignedDeviceId() {
        this.storage.remove(SIGNED_DEVICE_ID);
    }

    public String getSignedDeviceId() {
        return this.storage.get(SIGNED_DEVICE_ID);
    }
}
