package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.spec.SecretKeySpec;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.crypto.IngCryptoUtils;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IngStorage {

    public static final String NO_KEY_AVAILABLE = "No key available";

    private final PersistentStorage persistentStorage;

    private final SessionStorage sessionStorage;

    private final IngCryptoUtils ingCryptoUtils;

    public IngStorage(
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            IngCryptoUtils ingCryptoUtils) {
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.ingCryptoUtils = ingCryptoUtils;
    }

    public void storeClientPrivateKey(PrivateKey privateKey) {
        sessionStorage.put(
                Storage.CLIENT_PRIVATE_KEY,
                EncodingUtils.encodeAsBase64String(privateKey.getEncoded()));
    }

    public PrivateKey getClientPrivateKey() {
        String encoded =
                sessionStorage
                        .get(Storage.CLIENT_PRIVATE_KEY, String.class)
                        .orElseThrow(() -> new IllegalStateException(NO_KEY_AVAILABLE));

        return ingCryptoUtils.getPrivateKeyFromBase64(encoded);
    }

    public void storeClientPublicKey(PublicKey publicKey) {
        sessionStorage.put(
                Storage.CLIENT_PUBLIC_KEY,
                EncodingUtils.encodeAsBase64String(publicKey.getEncoded()));
    }

    public PublicKey getClientPublicKey() {
        String encoded =
                sessionStorage
                        .get(Storage.CLIENT_PUBLIC_KEY, String.class)
                        .orElseThrow(() -> new IllegalStateException(NO_KEY_AVAILABLE));

        return ingCryptoUtils.getPublicKeyFromBase64(encoded);
    }

    public void storeServerPublicKey(PublicKey publicKey) {
        persistentStorage.put(
                Storage.SERVER_PUBLIC_KEY,
                EncodingUtils.encodeAsBase64String(publicKey.getEncoded()));
    }

    public PublicKey getServerPublicKey() {
        String encoded =
                persistentStorage
                        .get(Storage.SERVER_PUBLIC_KEY, String.class)
                        .orElseThrow(() -> new IllegalStateException(NO_KEY_AVAILABLE));

        return ingCryptoUtils.getPublicKeyFromBase64(encoded);
    }

    public void storeEncryptionKey(SecretKeySpec encryptionKey) {
        sessionStorage.put(
                Storage.ENCRYPTION_KEY,
                EncodingUtils.encodeAsBase64String(encryptionKey.getEncoded()));
    }

    public SecretKeySpec getEncryptionKey() {
        String encoded =
                sessionStorage
                        .get(Storage.ENCRYPTION_KEY, String.class)
                        .orElseThrow(() -> new IllegalStateException(NO_KEY_AVAILABLE));

        return ingCryptoUtils.getSecretKeyFromBase64(encoded);
    }

    public void storeSigningKey(SecretKeySpec signingKey) {
        sessionStorage.put(
                Storage.SIGNING_KEY, EncodingUtils.encodeAsBase64String(signingKey.getEncoded()));
    }

    public SecretKeySpec getSigningKey() {
        String encoded =
                sessionStorage
                        .get(Storage.SIGNING_KEY, String.class)
                        .orElseThrow(() -> new IllegalStateException(NO_KEY_AVAILABLE));

        return ingCryptoUtils.getSecretKeyFromBase64(encoded);
    }

    public void storeAccessToken(String accessToken) {
        sessionStorage.put(Storage.ACCESS_TOKEN, accessToken);
    }

    public String getAccessToken() {
        return sessionStorage.get(Storage.ACCESS_TOKEN);
    }

    public void storeEnrollPinningPrivateKey(PrivateKey privateKey) {
        persistentStorage.put(
                Storage.ENROLL_DEVICE_PINNING_PRIVKEY,
                EncodingUtils.encodeAsBase64String(privateKey.getEncoded()));
    }

    public PrivateKey getEnrollPinningPrivateKey() {
        String encoded =
                persistentStorage
                        .get(Storage.ENROLL_DEVICE_PINNING_PRIVKEY, String.class)
                        .orElseThrow(() -> new IllegalStateException(NO_KEY_AVAILABLE));

        return ingCryptoUtils.getPrivateKeyFromBase64(encoded);
    }

    public void storeEnrollPinningPublicKey(PublicKey publicKey) {
        persistentStorage.put(
                Storage.ENROLL_DEVICE_PINNING_PUBKEY,
                EncodingUtils.encodeAsBase64String(publicKey.getEncoded()));
    }

    public PublicKey getEnrollPinningPublicKey() {
        String encoded =
                persistentStorage
                        .get(Storage.ENROLL_DEVICE_PINNING_PUBKEY, String.class)
                        .orElseThrow(() -> new IllegalStateException(NO_KEY_AVAILABLE));

        return ingCryptoUtils.getPublicKeyFromBase64(encoded);
    }

    public void storeEnrollSigningPrivateKey(PrivateKey privateKey) {
        persistentStorage.put(
                Storage.ENROLL_SIGNING_PRIVKEY,
                EncodingUtils.encodeAsBase64String(privateKey.getEncoded()));
    }

    public PrivateKey getEnrollSigningPrivateKey() {
        String encoded =
                persistentStorage
                        .get(Storage.ENROLL_SIGNING_PRIVKEY, String.class)
                        .orElseThrow(() -> new IllegalStateException(NO_KEY_AVAILABLE));

        return ingCryptoUtils.getPrivateKeyFromBase64(encoded);
    }

    public void storeEnrollSigningPublicKey(PublicKey publicKey) {
        persistentStorage.put(
                Storage.ENROLL_SIGNING_PUBKEY,
                EncodingUtils.encodeAsBase64String(publicKey.getEncoded()));
    }

    public PublicKey getEnrollSigningPublicKey() {
        String encoded =
                persistentStorage
                        .get(Storage.ENROLL_SIGNING_PUBKEY, String.class)
                        .orElseThrow(() -> new IllegalStateException(NO_KEY_AVAILABLE));

        return ingCryptoUtils.getPublicKeyFromBase64(encoded);
    }

    public boolean wasEnrolled() {
        return persistentStorage.get(Storage.ENROLL_SIGNING_PRIVKEY, String.class).isPresent();
    }

    public void storePermanent(String key, String value) {
        persistentStorage.put(key, value);
    }

    public String getPermanent(String key) {
        return persistentStorage.get(key);
    }

    public void storeForSession(String key, String value) {
        sessionStorage.put(key, value);
    }

    public String getForSession(String key) {
        return sessionStorage.get(key);
    }
}
