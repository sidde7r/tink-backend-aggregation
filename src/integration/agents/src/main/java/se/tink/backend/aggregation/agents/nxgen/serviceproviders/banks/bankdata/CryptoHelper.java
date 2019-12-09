package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class CryptoHelper implements Decryptor {

    private static final String KEY_PAIR_ID_STORAGE = "KAY_PAIR_ID_STORAGE";
    private static final String PUBLIC_KEY_STORAGE = "PUBLIC_KEY_STORAGE";
    private static final String PRIVATE_KEY_STORAGE = "PRIVATE_KEY_STORAGE";
    private static final String SESSION_KEY_STORAGE = "SESSION_KEY_STORAGE";
    private static final String IV_STORAGE = "IV_STORAGE";

    private static final int IV_SIZE = 16;
    private static final int SESSION_KEY_SIZE = 32;
    private static final int RSA_KEY_SIZE = 2048;

    private final String keyPairId;
    private final KeyPair keyPair;
    private final byte[] sessionKey;
    private final byte[] iv;

    public CryptoHelper(final String keyPairId) {
        this(
                keyPairId,
                RSA.generateKeyPair(RSA_KEY_SIZE),
                randomBytes(SESSION_KEY_SIZE),
                randomBytes(IV_SIZE));
    }

    public CryptoHelper(String keyPairId, KeyPair keyPair, byte[] sessionKey, byte[] iv) {
        this.keyPairId = keyPairId;
        this.keyPair = keyPair;
        this.sessionKey = sessionKey;
        this.iv = iv;
    }

    // TODO: generalize a bit.
    public String enrollCrypt() {

        String publicKey = EncodingUtils.encodeAsBase64String(keyPair.getPublic().getEncoded());
        String jdata = buildJSONData(keyPairId, publicKey);

        byte[] actualAESInputDataInBytes = jdata.getBytes();
        byte[] encryptedData = AES.encryptCbcPkcs7(sessionKey, iv, actualAESInputDataInBytes);
        byte[] wholePackage = Bytes.concat(iv, encryptedData);
        return EncodingUtils.encodeAsBase64String(wholePackage);
    }

    public String getEncryptedSessionKey(RSAPublicKey publicKey) {
        return new String(
                Base64.encodeBase64(RSA.encryptNoneOaepMgf1(publicKey, sessionKey)),
                Charsets.UTF_8);
    }

    /**
     * Encrypt data with sessionKey and iv, and prepends the iv.
     *
     * @param data data to encrypt
     * @return String derived from: b64(iv + encrypt(data))
     */
    public String encrypt(byte[] data) {
        return new String(
                Base64.encodeBase64(Bytes.concat(iv, AES.encryptCbc(sessionKey, iv, data))));
    }

    /**
     * Decrypts given data using sessionKey and iv. Removes prepended iv.
     *
     * @param data String in b64 format
     * @return byte[] of encrypted data, with prepended iv removed.
     */
    @Override
    public byte[] decrypt(String data) {
        byte[] decryptedBytes = AES.decryptCbc(sessionKey, iv, Base64.decodeBase64(data));
        return Arrays.copyOfRange(decryptedBytes, IV_SIZE, decryptedBytes.length);
    }

    private static byte[] randomBytes(int size) {
        byte[] randomBytes = new byte[size];
        new SecureRandom().nextBytes(randomBytes);
        return randomBytes;
    }

    public void persist(final Storage storage) {
        storage.put(KEY_PAIR_ID_STORAGE, keyPairId);
        storage.put(
                PUBLIC_KEY_STORAGE, Base64.encodeBase64String(keyPair.getPublic().getEncoded()));
        storage.put(
                PRIVATE_KEY_STORAGE, Base64.encodeBase64String(keyPair.getPrivate().getEncoded()));
        storage.put(SESSION_KEY_STORAGE, Base64.encodeBase64String(sessionKey));
        storage.put(IV_STORAGE, Base64.encodeBase64String(iv));
    }

    public static Optional<CryptoHelper> load(final Storage storage) {

        if (!canLoad(storage)) {
            return Optional.empty();
        }

        final String keyPairId = storage.get(KEY_PAIR_ID_STORAGE);
        final String privateKeyB64 = storage.get(PRIVATE_KEY_STORAGE);
        final String publicKeyB64 = storage.get(PUBLIC_KEY_STORAGE);
        final String sessionKeyB64 = storage.get(SESSION_KEY_STORAGE);
        final String ivB64 = storage.get(IV_STORAGE);

        final KeyPair keyPair =
                new KeyPair(
                        RSA.getPubKeyFromBytes(Base64.decodeBase64(publicKeyB64)),
                        RSA.getPrivateKeyFromBytes(Base64.decodeBase64(privateKeyB64)));

        return Optional.of(
                new CryptoHelper(
                        keyPairId,
                        keyPair,
                        Base64.decodeBase64(sessionKeyB64),
                        Base64.decodeBase64(ivB64)));
    }

    private static boolean canLoad(final Storage storage) {
        return storage.containsKey(KEY_PAIR_ID_STORAGE)
                && storage.containsKey(PRIVATE_KEY_STORAGE)
                && storage.containsKey(PUBLIC_KEY_STORAGE)
                && storage.containsKey(SESSION_KEY_STORAGE)
                && storage.containsKey(IV_STORAGE);
    }

    // TODO: Bankdata usess another jsonlib than we do which escapes data differently.
    // TODO: We should probably find a way to configure our own lib to do this.
    private static String buildJSONData(String keyId, String publicKey) {
        /*
        ASCII data input string
        {
            "keyId": "93542267bdc44b999f5388a9994b9902",
            "publicKey": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv5tCBm3PQ27wbqKk2d/cwTQiUfB/ZHWPS+1gXkayNG9mjyo8VqrxBsLlsTiVhTs8mLo0xlTI/YRfesII8kmepukHoCXW7AvWvl5TtR96rReyzO8xKvJhi0Qonhb7Fr/fFQYNbInJVcWXGKb739/9SkBUhdcf3clTRdHSRkpcp5/TrkIestfAtpzJK2+rQkOfIamzcdRbQ4DJG1TL4CyxZwKkbPWj81cdTzTRW99sRuHDwexNojlOfN08YAvBPlkmTYa7eaZA8SZz1cZeAXSpG5zSrATKIdbE9hHl3Z3kJtUVwaYxE9z4f/aNQZFaSMdo+8mdQ4WD8ioCm7YcWSPxtwIDAQAB"
        }
         */
        StringBuilder sb = new StringBuilder();
        sb.append("{\"keyId\":\"");
        sb.append(keyId);
        sb.append("\"");
        sb.append(",");
        sb.append("\"publicKey\":\"");
        sb.append(publicKey);
        sb.append("\"");
        sb.append("}");
        return sb.toString();
    }
}
