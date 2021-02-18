package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6VerifierGenerator;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.exceptions.refresh.IdentityRefreshException;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.identity.entities.IdentityDataEntity;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Data
public class Sparebank1Identity {
    private static final String STORAGE_KEY_DEVICEID = "deviceId";
    private static final String STORAGE_KEY_KEY_PAIR = "keyPair";
    private static final String STORAGE_KEY_SALT = "salt";
    private static final String STORAGE_KEY_USERNAME = "userName";
    private static final String STORAGE_KEY_PASSWORD = "password";
    private static final String STORAGE_KEY_VERIFICATOR = "verificator";
    private static final String STORAGE_KEY_TOKEN = "token";
    private static final String STORAGE_IDENTITY_DATA = "identityData";

    private String deviceId;
    private KeyPair keyPair;
    private BigInteger salt;
    // userName is the user's public key encoded in base64
    private String userName;
    private String password;
    private BigInteger verificator;
    private String token;
    private IdentityDataEntity identityData;

    public static Sparebank1Identity create() {
        String deviceId = UUID.randomUUID().toString().toUpperCase();

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");

            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.genKeyPair();

            SRP6CryptoParams config = SRP6CryptoParams.getInstance(1024, "SHA-256");
            SRP6VerifierGenerator gen = new SRP6VerifierGenerator(config);
            BigInteger salt = new BigInteger(gen.generateRandomSalt(64)).abs();
            String userName = EncodingUtils.encodeAsBase64String(keyPair.getPublic().getEncoded());
            String password = String.valueOf(new SecureRandom().nextInt());
            BigInteger verificator = gen.generateVerifier(salt, userName, password);

            Sparebank1Identity identity = new Sparebank1Identity();
            identity.setDeviceId(deviceId);
            identity.setKeyPair(keyPair);
            identity.setSalt(salt);
            identity.setUserName(userName);
            identity.setPassword(password);
            identity.setVerificator(verificator);

            return identity;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public void save(PersistentStorage persistentStorage) {
        persistentStorage.put(STORAGE_KEY_DEVICEID, deviceId);
        persistentStorage.put(STORAGE_KEY_KEY_PAIR, SerializationUtils.serializeKeyPair(keyPair));
        persistentStorage.put(STORAGE_KEY_SALT, String.valueOf(salt));
        persistentStorage.put(STORAGE_KEY_USERNAME, userName);
        persistentStorage.put(STORAGE_KEY_PASSWORD, password);
        persistentStorage.put(STORAGE_KEY_VERIFICATOR, String.valueOf(verificator));
        persistentStorage.put(STORAGE_KEY_TOKEN, token);
        persistentStorage.put(STORAGE_IDENTITY_DATA, identityData);
    }

    public static Sparebank1Identity load(Map<String, String> persistentStorage) {
        Sparebank1Identity identity = new Sparebank1Identity();

        if (persistentStorage.containsKey(STORAGE_KEY_DEVICEID)) {
            identity.setDeviceId(persistentStorage.get(STORAGE_KEY_DEVICEID));
        }
        if (persistentStorage.containsKey(STORAGE_KEY_KEY_PAIR)) {
            identity.setKeyPair(
                    SerializationUtils.deserializeKeyPair(
                            persistentStorage.get(STORAGE_KEY_KEY_PAIR)));
        }
        if (persistentStorage.containsKey(STORAGE_KEY_SALT)) {
            identity.setSalt(new BigInteger(persistentStorage.get(STORAGE_KEY_SALT)));
        }
        if (persistentStorage.containsKey(STORAGE_KEY_USERNAME)) {
            identity.setUserName(persistentStorage.get(STORAGE_KEY_USERNAME));
        }
        if (persistentStorage.containsKey(STORAGE_KEY_PASSWORD)) {
            identity.setPassword(persistentStorage.get(STORAGE_KEY_PASSWORD));
        }
        if (persistentStorage.containsKey(STORAGE_KEY_VERIFICATOR)) {
            identity.setVerificator(new BigInteger(persistentStorage.get(STORAGE_KEY_VERIFICATOR)));
        }
        if (persistentStorage.containsKey(STORAGE_KEY_TOKEN)) {
            identity.setToken(persistentStorage.get(STORAGE_KEY_TOKEN));
        }

        return identity;
    }

    public boolean isAutoAuthenticationPossible() {
        return ObjectUtils.allNotNull(deviceId, token);
    }

    public static IdentityDataEntity loadIdentityData(PersistentStorage persistentStorage) {
        return persistentStorage
                .get(STORAGE_IDENTITY_DATA, IdentityDataEntity.class)
                .orElseThrow(
                        () ->
                                new IdentityRefreshException(
                                        " Identity data not retrieved from persistent storage"));
    }
}
