package se.tink.backend.aggregation.agents.banks.uk.barclays;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Hex;

public class BarclaysIdentity {

    private static final String STORAGE_KEY_QSA = "qsa";
    private static final String STORAGE_KEY_QSD = "qsd";
    private static final String STORAGE_KEY_DEVICE_ID = "deviceId";
    private static final String STORAGE_KEY_AID = "aId";

    // assigned by the client
    private String deviceId = "";

    // assigned by the server
    private String aId = "";

    // signing keys (only noted usage of qsa)
    private KeyPair qsa;
    private KeyPair qsd;

    // temporary
    private boolean isDemoUser(String deviceIdentifier) {
        byte[] bHash = BarclaysCrypto.sha256(deviceIdentifier.getBytes());
        String sHash = Hex.encodeHexString(bHash);
        return sHash.equals("4915ada4681bd54ea545a88b7339ceaf0e9eb4317a7bd4b943822d96ae1db3f5");
    }

    public BarclaysIdentity(String userId, /* temporary */ String deviceIdentifier) {

        if (isDemoUser(deviceIdentifier)) {
            // temporary use this deviceId for our demo user
            deviceId = "1bf2b5f31dfea71852545c10766e0fcff9632b872d9173832f7017df8a687fdf";
        } else {
            // The bank has a set limit of how many unique deviceIds we are allowed to
            // use. Base it on userId in order to not lock the user's account.
            byte[] bDeviceId = BarclaysCrypto.sha256(userId.getBytes());
            deviceId = Hex.encodeHexString(bDeviceId);
        }

        qsa = BarclaysCrypto.ecGenerateKeyPair();
        qsd = BarclaysCrypto.ecGenerateKeyPair();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getaId() {
        return aId;
    }

    public void setaId(String aId) {
        this.aId = aId;
    }

    public KeyPair getQsa() {
        return qsa;
    }

    public KeyPair getQsd() {
        return qsd;
    }

    public Map<String, String> save() {
        Map<String,String> storage = new HashMap<String,String>();
        storage.put(STORAGE_KEY_QSA, BarclaysCrypto.serializeKeyPair(qsa));
        storage.put(STORAGE_KEY_QSD, BarclaysCrypto.serializeKeyPair(qsd));
        storage.put(STORAGE_KEY_DEVICE_ID, deviceId);
        storage.put(STORAGE_KEY_AID, aId);
        return storage;
    }

    public void load(Map<String,String> storage) {
        if (storage.containsKey(STORAGE_KEY_QSA)) {
            qsa = BarclaysCrypto.deserializeKeyPair(storage.get(STORAGE_KEY_QSA));
        }
        if (storage.containsKey(STORAGE_KEY_QSD)) {
            qsd = BarclaysCrypto.deserializeKeyPair(storage.get(STORAGE_KEY_QSD));
        }
        if (storage.containsKey(STORAGE_KEY_DEVICE_ID)) {
            deviceId = storage.get(STORAGE_KEY_DEVICE_ID);
        }
        if (storage.containsKey(STORAGE_KEY_AID)) {
            aId = storage.get(STORAGE_KEY_AID);
        }
    }
}
