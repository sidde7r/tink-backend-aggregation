package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank;

import java.math.BigInteger;
import java.security.KeyPair;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.Storage;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@AllArgsConstructor
public class JyskeBankPersistentStorage {
    private final PersistentStorage persistentStorage;

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

    public void setKeyPair(KeyPair keyPair) {
        persistentStorage.put(Storage.KEY_PAIR, SerializationUtils.serializeKeyPair(keyPair));
    }

    public KeyPair getKeyPair() {
        return SerializationUtils.deserializeKeyPair(persistentStorage.get(Storage.KEY_PAIR));
    }

    public void setClientId(String clientId) {
        persistentStorage.put(Storage.CLIENT_ID, clientId);
    }

    public String getClientId() {
        return persistentStorage.get(Storage.CLIENT_ID);
    }

    public void setClientSecret(String clientSecret) {
        persistentStorage.put(Storage.CLIENT_SECRET, clientSecret);
    }

    public String getClientSecret() {
        return persistentStorage.get(Storage.CLIENT_SECRET);
    }

    public String getCount() {
        BigInteger count =
                persistentStorage
                        .get(Storage.COUNTER, BigInteger.class)
                        .orElse(BigInteger.ZERO)
                        .add(BigInteger.ONE);
        persistentStorage.put(Storage.COUNTER, count);

        return count.toString();
    }
}
