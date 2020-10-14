package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.crypto;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.agreement.srp.SRP6Client;
import org.bouncycastle.crypto.agreement.srp.SRP6VerifierGenerator;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class IngCryptoUtils {

    private static final String CANNOT_DECODE_KEY = "Cannot decode key";

    private static final String HMAC_SHA512 = "HmacSHA512";

    private static final String PRIME_256V1 = "prime256v1";

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int ENCRYPTION_KEY_LENGTH = 32;
    private static final int SIGNING_KEY_LENGTH = 64;
    private static final int IV_LEN_BYTES = 12;

    public byte[] getRandomBytes(int numBytes) {
        byte[] bytes = new byte[numBytes];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    public String getBase64RandomBytes(int numBytes) {
        return Base64.getEncoder().encodeToString(getRandomBytes(numBytes));
    }

    public KeyPair generateKeys() {
        return EllipticCurve.generateKeyPair(PRIME_256V1);
    }

    public DerivedKeyOutput deriveKeys(
            PrivateKey privateKey, PublicKey publicKey, byte[] salt, byte[] context) {
        byte[] sharedSecret = EllipticCurve.diffieHellmanDeriveKey(privateKey, publicKey);
        return deriveKeys(sharedSecret, salt, context);
    }

    public DerivedKeyOutput deriveKeys(byte[] secret, byte[] salt, byte[] context) {
        byte[] first = new byte[ENCRYPTION_KEY_LENGTH];
        byte[] second = new byte[SIGNING_KEY_LENGTH];

        HKDFBytesGenerator generator = new HKDFBytesGenerator(new SHA256Digest());
        HKDFParameters params = new HKDFParameters(secret, salt, context);
        generator.init(params);
        generator.generateBytes(first, 0, ENCRYPTION_KEY_LENGTH);
        generator.generateBytes(second, 0, SIGNING_KEY_LENGTH);

        return new DerivedKeyOutput(
                new SecretKeySpec(first, HMAC_SHA512), new SecretKeySpec(second, HMAC_SHA512));
    }

    public byte[] calculateSignature(byte[] message, SecretKeySpec signingKey) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA512);
            mac.init(signingKey);
            return mac.doFinal(message);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Could not calculate signature", e);
        }
    }

    public PrivateKey getPrivateKeyFromBase64(String encodedPrivateKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(EncodingUtils.decodeBase64String(encodedPrivateKey)));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(CANNOT_DECODE_KEY, e);
        }
    }

    public PublicKey getPublicKeyFromBase64(String encodedPublicKey) {
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePublic(
                    new PKCS8EncodedKeySpec(EncodingUtils.decodeBase64String(encodedPublicKey)));
        } catch (InvalidKeySpecException e) {
            try {
                return keyFactory.generatePublic(
                        new X509EncodedKeySpec(EncodingUtils.decodeBase64String(encodedPublicKey)));
            } catch (InvalidKeySpecException e2) {
                throw new IllegalStateException(CANNOT_DECODE_KEY, e);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(CANNOT_DECODE_KEY, e);
        }
    }

    public SecretKeySpec getSecretKeyFromBase64(String encodedKey) {
        return new SecretKeySpec(EncodingUtils.decodeBase64String(encodedKey), HMAC_SHA512);
    }

    public String generateSRP6Verifier(byte[] salt, String mobileAppId, byte[] password) {

        SRP6VerifierGenerator srp6VerifierGenerator = new SRP6VerifierGenerator();
        srp6VerifierGenerator.init(
                new BigInteger(1, IngConstants.SAFE_PRIME),
                new BigInteger(1, IngConstants.GROUP_PARAM),
                new SHA256Digest());

        BigInteger verifier =
                srp6VerifierGenerator.generateVerifier(salt, mobileAppId.getBytes(), password);

        String rawVerifier = EncodingUtils.encodeHexAsString(verifier.toByteArray()).toUpperCase();
        return removePaddingZeros(rawVerifier);
    }

    public SRP6ClientValues generateSRP6ClientValues(
            String serverPublicValue, String deviceSalt, String mobileAppId, byte[] password) {

        try {
            SRP6Client srp6Client = new SRP6Client();
            srp6Client.init(
                    new BigInteger(1, IngConstants.SAFE_PRIME),
                    new BigInteger(1, IngConstants.GROUP_PARAM),
                    new SHA256Digest(),
                    RANDOM);

            byte[] salt = EncodingUtils.decodeHexString(deviceSalt);
            byte[] identity = mobileAppId.getBytes();

            BigInteger clientCreds = srp6Client.generateClientCredentials(salt, identity, password);
            BigInteger secret =
                    srp6Client.calculateSecret(
                            new BigInteger(1, EncodingUtils.decodeHexString(serverPublicValue)));
            BigInteger clientEvidence = srp6Client.calculateClientEvidenceMessage();

            return new SRP6ClientValues(clientCreds, clientEvidence, secret);
        } catch (CryptoException e) {
            throw new IllegalStateException("Could not calculate client evidence message", e);
        }
    }

    public String getClientEvidenceSignature(byte[] evidenceMessage, PrivateKey privateKey) {
        byte[] bytes = Hash.sha512(evidenceMessage);
        byte[] signature = EllipticCurve.signNone(privateKey, bytes);

        return Base64.getEncoder().encodeToString(signature);
    }

    public boolean verifyEvidence(byte[] evidenceMessage, String signature, PublicKey publicKey) {
        byte[] bytes = Hash.sha512(evidenceMessage);

        return EllipticCurve.verifySignNone(
                publicKey, bytes, Base64.getDecoder().decode(signature));
    }

    public String decryptExtra(String extraEncodedAndEncrypted, SecretKey encryptionKey)
            throws GeneralSecurityException {
        byte[] decoded = EncodingUtils.decodeBase64String(extraEncodedAndEncrypted);

        Cipher instance = Cipher.getInstance("AES/GCM/NoPadding", "BC");

        GCMParameterSpec gCMParameterSpec = new GCMParameterSpec(128, decoded, 0, IV_LEN_BYTES);
        instance.init(Cipher.DECRYPT_MODE, encryptionKey, gCMParameterSpec);
        byte[] bytes = instance.doFinal(decoded, IV_LEN_BYTES, decoded.length - IV_LEN_BYTES);
        return new String(bytes);
    }

    private String removePaddingZeros(String input) {
        return input.replaceFirst("^00", "");
    }
}
