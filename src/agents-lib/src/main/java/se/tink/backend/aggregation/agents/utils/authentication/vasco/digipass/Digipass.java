package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;
import java.security.KeyPair;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.models.DecryptActivationDataResponse;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.models.InitializeRegistrationDataResponse;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.models.OtpChallengeResponse;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils.CryptoUtils;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils.FingerPrintUtils;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils.OtpUtils;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils.XfadUtils;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.libraries.serialization.TypeReferences;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Digipass {
    private int otpCounter = 0;
    private byte[] otpKey;

    // Generated by client
    private String fingerPrint;
    private KeyPair ellipticCurveKeyPair;
    private byte[] clientNonce;
    private byte[] ecdhSharedSecret;

    // Generated by server
    private String activationPassword;
    private byte[] xfad;
    private byte[] otpSeedIv;
    private byte[] otpSeedData;
    private StaticVector staticVector;

    // First step
    public InitializeRegistrationDataResponse initializeRegistrationData(String activationPassword) {
        this.fingerPrint = FingerPrintUtils.generateFingerPrint();
        this.ellipticCurveKeyPair = EllipticCurve.generateKeyPair(DigipassConstants.CommunicationCrypto.EC_CURVE_NAME);

        this.activationPassword = activationPassword;
        this.clientNonce = RandomUtils.secureRandom(DigipassConstants.CommunicationCrypto.NONCE_LENGTH);

        byte[] activationKey = CryptoUtils.deriveActivationKey(this.activationPassword);
        byte[] iv = RandomUtils.secureRandom(DigipassConstants.CommunicationCrypto.IV_LENGTH);

        byte[] encryptedPublicKeyAndNonce = CryptoUtils.encryptPublicKeyAndNonce(
                activationKey,
                iv,
                this.ellipticCurveKeyPair,
                this.clientNonce
        );

        return new InitializeRegistrationDataResponse(iv, encryptedPublicKeyAndNonce);
    }

    // Second step
    public DecryptActivationDataResponse decryptActivationData(String serverInitialVector, String encryptedNonces,
            String encryptedServerPublicKey, String xfad, String challenge) {
        Preconditions.checkNotNull(this.fingerPrint, "Fingerprint has not been initialized.");
        Preconditions.checkNotNull(this.ellipticCurveKeyPair, "ECKey has not been initialized.");
        Preconditions.checkNotNull(this.activationPassword, "ActivationPassword has not been initialized.");

        byte[] requestIv = EncodingUtils.decodeHexString(serverInitialVector);
        byte[] responseIv = RandomUtils.secureRandom(DigipassConstants.CommunicationCrypto.IV_LENGTH);

        this.ecdhSharedSecret = calculateSharedSecret(requestIv, encryptedServerPublicKey);

        byte[] encryptedServerNonce = CryptoUtils.reencryptServerNonce(
                this.ecdhSharedSecret,
                requestIv,
                responseIv,
                EncodingUtils.decodeHexString(encryptedNonces)
        );

        byte[] decryptedXfad = AES.decryptCbc(
                this.ecdhSharedSecret,
                requestIv,
                EncodingUtils.decodeHexString(xfad)
        );
        // The decrypted Xfad is hex encoded (i.e. a string).
        loadXfad(new String(decryptedXfad));

        calculateOtpKey();

        String otpResponse = calculateOtp(0, challenge);
        String derivationCode = calculateDerivationCode(otpResponse);

        return new DecryptActivationDataResponse(responseIv, encryptedServerNonce, derivationCode);
    }

    // Third step (every auth)
    public OtpChallengeResponse generateResponseFromChallenge(String challenge) {
        Preconditions.checkNotNull(this.fingerPrint, "Fingerprint has not been initialized.");
        Preconditions.checkNotNull(this.ellipticCurveKeyPair, "ECKey has not been initialized.");
        Preconditions.checkNotNull(this.otpKey, "OTP key has not been initialized.");
        Preconditions.checkNotNull(this.staticVector, "Static vector has not been initialized.");

        String otpResponse = calculateOtp(otpCounter++, challenge);

        return new OtpChallengeResponse(otpResponse);
    }

    private byte[] calculateSharedSecret(byte[] iv, String encryptedServerPublicKey) {
        Preconditions.checkNotNull(this.activationPassword, "Activation password not set.");

        byte[] activationKey = CryptoUtils.deriveActivationKey(this.activationPassword);

        ECPublicKey serverPublicKey = CryptoUtils.decryptPublicKey(
                DigipassConstants.CommunicationCrypto.EC_CURVE_NAME,
                activationKey,
                iv,
                EncodingUtils.decodeHexString(encryptedServerPublicKey)
        );

        return CryptoUtils.calculateSharedSecret(
                (ECPrivateKey) ellipticCurveKeyPair.getPrivate(),
                serverPublicKey
        );
    }

    private void calculateOtpKey() {
        Preconditions.checkNotNull(this.staticVector, "StaticVector not initialized.");
        Preconditions.checkNotNull(this.otpSeedIv, "otpSeedIv not initialized.");
        Preconditions.checkNotNull(this.otpSeedData, "otpSeedData not initialized.");

        byte[] key = staticVector.getMandatoryField(DigipassConstants.StaticVectorFieldType.CRYPTO_KEY);
        byte[] signature = staticVector.getMandatoryField(DigipassConstants.StaticVectorFieldType.SIGNATURE);
        byte[] iv = Bytes.concat(signature, this.otpSeedIv);

        this.otpKey = CryptoUtils.calculateOtpKey(key, iv, this.otpSeedData);
    }

    private void loadXfad(String xfadHex) {
        // Make sure we store the original xfad
        this.xfad = EncodingUtils.decodeHexString(xfadHex);

        this.otpSeedIv = XfadUtils.getOtpSeedIv(this.xfad);
        this.otpSeedData = XfadUtils.getOtpSeedData(this.xfad);

        this.staticVector = StaticVector.createFromXfad(this.xfad);
    }

    private long getCurrentEpoch() {
        return Instant.now().getEpochSecond();
    }

    private String calculateOtp(int otpCounter, List<byte[]> challenges) {
        long epochTime = getCurrentEpoch();
        return OtpUtils.calculateOtp(
                this.fingerPrint,
                this.staticVector,
                this.otpKey,
                otpCounter,
                epochTime,
                challenges
        );
    }

    private String calculateOtp(int otpCounter, String challenge) {
        return calculateOtp(otpCounter, Collections.singletonList(EncodingUtils.decodeHexString(challenge)));
    }

    private String calculateDerivationCode(String otpResponse) {
        return OtpUtils.calculateDerivationCode(this.fingerPrint, this.staticVector, otpResponse);
    }

    public String serialize() {
        Map<String, String> m = new HashMap<>();

        m.put(DigipassConstants.Serialization.OTP_COUNTER, Integer.toString(otpCounter));
        Optional.ofNullable(otpKey)
                .ifPresent(v -> m.put(DigipassConstants.Serialization.OTP_KEY,
                        EncodingUtils.encodeAsBase64String(v)));
        Optional.ofNullable(clientNonce)
                .ifPresent(v -> m.put(DigipassConstants.Serialization.CLIENT_NONCE,
                        EncodingUtils.encodeAsBase64String(v)));
        Optional.ofNullable(ecdhSharedSecret)
                .ifPresent(v -> m.put(DigipassConstants.Serialization.ECDH_SHARED_SECRET,
                        EncodingUtils.encodeAsBase64String(v)));
        Optional.ofNullable(xfad)
                .ifPresent(v -> m.put(DigipassConstants.Serialization.XFAD,
                        EncodingUtils.encodeAsBase64String(v)));
        Optional.ofNullable(otpSeedIv)
                .ifPresent(v -> m.put(DigipassConstants.Serialization.OTP_SEED_IV,
                        EncodingUtils.encodeAsBase64String(v)));
        Optional.ofNullable(otpSeedData)
                .ifPresent(v -> m.put(DigipassConstants.Serialization.OTP_SEED_DATA,
                        EncodingUtils.encodeAsBase64String(v)));
        Optional.ofNullable(fingerPrint)
                .ifPresent(v -> m.put(DigipassConstants.Serialization.FINGERPRINT, v));
        Optional.ofNullable(activationPassword)
                .ifPresent(v -> m.put(DigipassConstants.Serialization.ACTIVATION_PASSWORD, v));
        Optional.ofNullable(ellipticCurveKeyPair)
                .ifPresent(v -> m.put(DigipassConstants.Serialization.EC_KEY_PAIR,
                        SerializationUtils.serializeKeyPair(v)));

        return SerializationUtils.serializeToString(m);
    }

    private Optional<String> getIfExists(Map<String, String> m, String key) {
        if (!m.containsKey(key)) {
            return Optional.empty();
        }
        return Optional.ofNullable(m.get(key));
    }

    public void deserialize(String mstring) {
        Map<String, String> m = SerializationUtils.deserializeFromString(
                mstring,
                TypeReferences.MAP_OF_STRING_STRING
        );

        getIfExists(m, DigipassConstants.Serialization.OTP_COUNTER)
                .ifPresent(v-> otpCounter = Integer.valueOf(v));
        getIfExists(m, DigipassConstants.Serialization.OTP_KEY)
                .ifPresent(v-> otpKey = EncodingUtils.decodeBase64String(v));
        getIfExists(m, DigipassConstants.Serialization.CLIENT_NONCE)
                .ifPresent(v-> clientNonce = EncodingUtils.decodeBase64String(v));
        getIfExists(m, DigipassConstants.Serialization.ECDH_SHARED_SECRET)
                .ifPresent(v-> ecdhSharedSecret = EncodingUtils.decodeBase64String(v));
        getIfExists(m, DigipassConstants.Serialization.XFAD)
                .ifPresent(v-> xfad = EncodingUtils.decodeBase64String(v));
        getIfExists(m, DigipassConstants.Serialization.OTP_SEED_IV)
                .ifPresent(v-> otpSeedIv = EncodingUtils.decodeBase64String(v));
        getIfExists(m, DigipassConstants.Serialization.OTP_SEED_DATA)
                .ifPresent(v-> otpSeedData = EncodingUtils.decodeBase64String(v));
        getIfExists(m, DigipassConstants.Serialization.FINGERPRINT)
                .ifPresent(v-> fingerPrint = v);
        getIfExists(m, DigipassConstants.Serialization.ACTIVATION_PASSWORD)
                .ifPresent(v-> activationPassword = v);
        getIfExists(m, DigipassConstants.Serialization.EC_KEY_PAIR)
                .ifPresent(v-> ellipticCurveKeyPair = SerializationUtils.deserializeKeyPair(v));

        if (Objects.nonNull(this.xfad)) {
            this.staticVector = StaticVector.createFromXfad(this.xfad);
        }
    }
}
