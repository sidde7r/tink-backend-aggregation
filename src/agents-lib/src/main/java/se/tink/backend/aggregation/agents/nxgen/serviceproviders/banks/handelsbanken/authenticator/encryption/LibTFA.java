package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.encryption;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.DeviceInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.HandshakeDecoded;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.PdeviceSignContainer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.UserCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.HandshakeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.CreateProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.InitNewProfileResponse;
import se.tink.backend.agents.rpc.Credentials;

/**
 * Port of {@link se.tink.backend.aggregation.agents.banks.handelsbanken.v6.tfa.LibTFA}
 * Based on native C code in Handelsbanken app
 * Careful! This is a stateful object!
 */
public class LibTFA {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String deviceSecurityContextId;
    private final KeyPair deviceRsakey;

    private KeyPair handshakeKey;
    private byte[] clientNonce;
    private byte[] serverNonce;

    public LibTFA() {
        this(generateDeviceRSAKey(), createDeviceSecurityContextId());
    }

    public LibTFA(String serializedRsaPrivateKey, String deviceSecurityContextId) {
        this(createKeyPair(serializedRsaPrivateKey), deviceSecurityContextId);
    }

    private LibTFA(KeyPair deviceRsakey, String deviceSecurityContextId) {
        this.deviceRsakey = deviceRsakey;
        this.deviceSecurityContextId = deviceSecurityContextId;
    }

    private static KeyPair generateDeviceRSAKey() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);

            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw asIllegalState(e);
        }
    }

    private static String createDeviceSecurityContextId() {
        byte[] seed = new byte[32];
        HandelsbankenConstants.RANDOM.nextBytes(seed);
        return createDeviceSecurityContextId(seed);
    }

    private static KeyPair createKeyPair(String serializedPrivateKey) {
        byte[] privateKey = decode(serializedPrivateKey);
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privKey = kf.generatePrivate(keySpec);

            return new KeyPair(null, privKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw asIllegalState(e);
        }
    }

    private static String createDeviceSecurityContextId(byte[] seed) {
        try {
            // The Device Security Context Id must be:
            //  - 32 bytes long
            //  - The same for both activation and login
            return encodeAsString(hash(seed));
        } catch (NoSuchAlgorithmException e) {
            throw asIllegalState(e);
        }
    }

    public String generateNewClientNonce() {
        clientNonce = new byte[32];
        HandelsbankenConstants.RANDOM.nextBytes(clientNonce);
        return encodeAsString(this.clientNonce);
    }

    public String generateHandshakeAndGetPublicKey() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);

            handshakeKey = keyGen.generateKeyPair();

            return getPubkey(handshakeKey);
        } catch (NoSuchAlgorithmException e) {
            throw asIllegalState(e);
        }
    }

    public String getDeviceSecurityContextId() {
        return deviceSecurityContextId;
    }

    public String generateEncUserCredentials(
            InitNewProfileResponse initNewProfile,
            UserCredentialsRequest userCredentialsRequest) {
        serverNonce = decryptServerNonce(initNewProfile.getSnonce());
        return encryptAndEncodeBase64(userCredentialsRequest);
    }

    public String generateEncUserCredentials(
            HandshakeResponse handshake,
            UserCredentialsRequest userCredentialsRequest) {
        try {
            HandshakeDecoded handshakeDecoded = MAPPER.readValue(
                    new String(decryptServerNonce(handshake.getServerHello()), Charsets.US_ASCII),
                    HandshakeDecoded.class);
            serverNonce = decode(handshakeDecoded.getSnonce());
            return encryptAndEncodeBase64(userCredentialsRequest);
        } catch (IOException e) {
            throw asIllegalState(e);
        }
    }

    public String encryptAndEncodeBase64(Object object) {
        checkServerNonceAvailable();
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
            IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(this.serverNonce, 0, 16));
            SecretKey keyValue = new SecretKeySpec(calculateNewSessionKey(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keyValue, ivSpec);

            return HandelsbankenConstants.BASE64_CODEC.encodeToString(cipher.doFinal(MAPPER.writeValueAsBytes(object)));
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException |
                NoSuchPaddingException | BadPaddingException | NoSuchProviderException | IllegalBlockSizeException
                | JsonProcessingException e) {
            throw asIllegalState(e);
        }
    }

    private byte[] decryptServerNonce(String snonce) {
        Preconditions.checkNotNull(handshakeKey, "Please generate handshake key first.");
        try {
            Cipher cipher = Cipher.getInstance("RSA");

            cipher.init(Cipher.DECRYPT_MODE, handshakeKey.getPrivate());
            return cipher.doFinal(decode(snonce));
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException |
                IllegalBlockSizeException e) {
            throw asIllegalState(e);
        } finally {
            handshakeKey = null; // making sure we have a new one every time!
        }
    }

    private byte[] calculateNewSessionKey() {
        Preconditions.checkNotNull(clientNonce, "Please generate client nonce first.");
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(serverNonce, "HmacSHA256");
            hmac.init(secret_key);

            for (int i = 0; i < 50000; i++) {
                hmac.update(clientNonce);
            }

            return hmac.doFinal();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw asIllegalState(e);
        }
        // Not unsetting clientNonce, because it's reused for AuthTP=3
    }

    public PdeviceSignContainer generatePDeviceSignContainer(
            CreateProfileResponse createProfile) {
        return generatePDeviceSignContainer(createProfile.getChallenge());
    }

    public PdeviceSignContainer generatePDeviceSignContainer(ChallengeResponse challenge) {
        return generatePDeviceSignContainer(challenge.getChallenge());
    }

    private PdeviceSignContainer generatePDeviceSignContainer(String challenge) {
        try {
            DeviceInfoEntity deviceInfo = new DeviceInfoEntity(deviceSecurityContextId);
            byte[] deviceInfoSerialized = MAPPER.writeValueAsBytes(deviceInfo);
            return new PdeviceSignContainer()
                    .setSignature(signature(deviceInfoSerialized, challenge))
                    .setDeviceInfo(encodeAsString(deviceInfoSerialized));
        } catch (JsonProcessingException e) {
            throw asIllegalState(e);
        }
    }

    private String signature(byte[] data, String challenge) {
        checkServerNonceAvailable();
        try {
            Signature s = Signature.getInstance("SHA256withRSA");
            s.initSign(deviceRsakey.getPrivate());
            s.update(serverNonce);
            s.update(decode(challenge));
            s.update(hash(data));
            byte[] signature = s.sign();

            return encodeAsString(signature);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw asIllegalState(e);
        } finally {
            serverNonce = null;
        }
    }

    private void checkServerNonceAvailable() {
        Preconditions.checkNotNull(serverNonce, "Please get a server nonce first.");
    }

    private static byte[] hash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest h = MessageDigest.getInstance("SHA-256");
        return h.digest(data);
    }

    public String getDeviceRsaPublicKey() {
        return getPubkey(deviceRsakey);
    }

    public String getDeviceRsaPrivateKey() {
        return encodeAsString(deviceRsakey.getPrivate().getEncoded());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        LibTFA libTFA = (LibTFA) o;

        return new EqualsBuilder()
                .append(deviceSecurityContextId, libTFA.deviceSecurityContextId)
                .append(getDeviceRsaPrivateKey(), libTFA.getDeviceRsaPrivateKey())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(deviceSecurityContextId)
                .append(getDeviceRsaPrivateKey())
                .toHashCode();
    }

    private static IllegalStateException asIllegalState(Exception e) {
        return new IllegalStateException(e.getMessage(), e);
    }

    private static String getPubkey(KeyPair key) {
        return encodeAsString(key.getPublic().getEncoded());
    }

    private static String encodeAsString(byte[] data) {
        return HandelsbankenConstants.BASE64_CODEC.encodeAsString(data);
    }

    private static byte[] decode(String challenge) {
        return HandelsbankenConstants.BASE64_CODEC.decode(challenge);
    }

    public static String createDeviceSecurityContextId(Credentials credentials) {
        return createDeviceSecurityContextId(credentials.getId().getBytes(Charsets.UTF_8));
    }
}
