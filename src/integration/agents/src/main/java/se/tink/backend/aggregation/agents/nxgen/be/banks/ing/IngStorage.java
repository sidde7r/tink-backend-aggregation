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

    public void storeRefreshToken(String accessToken) {
        sessionStorage.put(Storage.REFRESH_TOKEN, accessToken);
    }

    public String getRefreshToken() {
        return sessionStorage.get(Storage.REFRESH_TOKEN);
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

    public void storeMobileAppId(String mobileAppId) {
        persistentStorage.put(Storage.MOBILE_APP_ID, mobileAppId);
    }

    public String getMobileAppId() {
        return persistentStorage.get(Storage.MOBILE_APP_ID);
    }

    public void storeSRP6Password(String password) {
        persistentStorage.put(Storage.SRP6_PASSWORD, password);
    }

    public String getSRP6Password() {
        return persistentStorage.get(Storage.SRP6_PASSWORD);
    }

    public void storeChallenge(String challenge) {
        sessionStorage.put(Storage.CHALLENGE, challenge);
    }

    public String getChallenge() {
        return sessionStorage.get(Storage.CHALLENGE);
    }

    public void storeBasketId(String basketId) {
        sessionStorage.put(Storage.BASKET_ID, basketId);
    }

    public String getBasketId() {
        return sessionStorage.get(Storage.BASKET_ID);
    }

    public void storeOtp(String otp) {
        sessionStorage.put(Storage.OTP, otp);
    }

    public String getOtp() {
        return sessionStorage.get(Storage.OTP);
    }

    public void storeDeviceSalt(String deviceSalt) {
        persistentStorage.put(Storage.DEVICE_SALT, deviceSalt);
    }

    public String getDeviceSalt() {
        return persistentStorage.get(Storage.DEVICE_SALT);
    }

    public void storeMpinSalt(String mpinSalt) {
        persistentStorage.put(Storage.MPIN_SALT, mpinSalt);
    }

    public String getMpinSalt() {
        return persistentStorage.get(Storage.MPIN_SALT);
    }
}
