package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank;

import javax.crypto.SecretKey;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.Storage;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class JyskeBankPersistentStorage {
    private final PersistentStorage persistentStorage;

    public JyskeBankPersistentStorage(PersistentStorage storage) {
        this.persistentStorage = storage;
    }

    public void setUserId(String userId) {
        persistentStorage.put(Storage.USER_ID, userId);
    }

    public String getUserId() {
        return persistentStorage.get(Storage.USER_ID);
    }

    public void setPincode(String pincode) {
        byte[] digest = Hash.sha256(pincode);
        persistentStorage.put(Storage.PIN_CODE, EncodingUtils.encodeAsBase64String(digest));
    }

    public String getPincode() {
        return persistentStorage.get(Storage.PIN_CODE);
    }

    public void setKeyId(String keyId) {
        persistentStorage.put(Storage.KEY_ID, keyId);
    }

    public String getKeyId() {
        return persistentStorage.get(Storage.KEY_ID);
    }

    public void setPublicKey(String publicKeyAsBase64) {
        persistentStorage.put(Storage.PUBLIC_KEY, publicKeyAsBase64);
    }

    public String getPublicKey() {
        return persistentStorage.get(Storage.PUBLIC_KEY);
    }

    public void setPrivateKey(String privateKeyAsBase64) {
        persistentStorage.put(Storage.PRIVATE_KEY, privateKeyAsBase64);
    }

    public void setAesKey(SecretKey cek) {
        persistentStorage.put(Storage.AES_KEY, cek);
    }

    public String getPrivateKey() {
        return persistentStorage.get(Storage.PRIVATE_KEY);
    }
}
