package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6VerifierGenerator;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.strings.StringUtils;

public class Sparebank1Identity {
    private static final String STORAGE_KEY_DEVICEID = "deviceId";
    private static final String STORAGE_KEY_KEY_PAIR = "keyPair";
    private static final String STORAGE_KEY_SALT = "salt";
    private static final String STORAGE_KEY_USERNAME = "userName";
    private static final String STORAGE_KEY_PASSWORD = "password";
    private static final String STORAGE_KEY_VERIFICATOR = "verificator";
    private static final String STORAGE_KEY_TOKEN = "token";

    private String deviceId;
    private KeyPair keyPair;
    private BigInteger salt;
    // userName is the user's public key encoded in base64
    private String userName;
    private String password;
    private BigInteger verificator;
    private String token;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public BigInteger getSalt() {
        return salt;
    }

    public void setSalt(BigInteger salt) {
        this.salt = salt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BigInteger getVerificator() {
        return verificator;
    }

    public void setVerificator(BigInteger verificator) {
        this.verificator = verificator;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static Sparebank1Identity create(String username) {
        String deviceId = StringUtils.hashAsUUID("TINK-" + username);

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
}
