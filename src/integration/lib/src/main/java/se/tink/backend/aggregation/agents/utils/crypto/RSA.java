package se.tink.backend.aggregation.agents.utils.crypto;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.function.Consumer;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class RSA {
    private static final String ALGORITHM = "RSA";

    public static KeyPair generateKeyPair(int keySize) {
        return generateKeyPair((keyPairGenerator -> keyPairGenerator.initialize(keySize)));
    }

    // Using algorithm defaults
    public static KeyPair generateKeyPair() {
        return generateKeyPair((keyPairGenerator -> {}));
    }

    private static KeyPair generateKeyPair(Consumer<KeyPairGenerator> initFunc) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            initFunc.accept(keyGen);
            return keyGen.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException();
        }
    }

    public static RSAPublicKey getPubKeyFromBytes(byte[] keyBytes) {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);

        return getRsaPublicKey(keySpec);
    }

    public static RSAPublicKey getPublicKeyFromModulusAndExponent(
            byte[] modulusBytes, byte[] exponentBytes) {
        BigInteger modulus = new BigInteger(1, modulusBytes);
        BigInteger publicExponent = new BigInteger(exponentBytes);
        return getPublicKeyFromModulusAndExponent(modulus, publicExponent);
    }

    public static RSAPublicKey getPublicKeyFromModulusAndExponent(
            BigInteger modulus, BigInteger publicExponent) {
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, publicExponent);

        return getRsaPublicKey(keySpec);
    }

    private static RSAPublicKey getRsaPublicKey(KeySpec keySpec) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @deprecated Use se.tink.backend.aggregation.agents.utils.crypto.Pem instead
     * @param keyBytes
     * @return
     */
    @Deprecated
    public static RSAPrivateKey getPrivateKeyFromBytes(byte[] keyBytes) {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        try {
            KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
            return (RSAPrivateKey) kf.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static byte[] encrypt(String cipherDefinition, RSAPublicKey publicKey, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(cipherDefinition);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);
        } catch (InvalidKeyException
                | BadPaddingException
                | IllegalBlockSizeException
                | NoSuchPaddingException
                | NoSuchAlgorithmException e) {
            throw new SecurityException(e.getMessage(), e);
        }
    }

    public static byte[] encryptEcbPkcs1(RSAPublicKey publicKey, byte[] data) {
        return encrypt("RSA/ECB/PKCS1Padding", publicKey, data);
    }

    public static byte[] encryptEcbNoPadding(RSAPublicKey publicKey, byte[] data) {
        return encrypt("RSA/ECB/NoPadding", publicKey, data);
    }

    public static byte[] encryptEcbOaepMgf1(RSAPublicKey publicKey, byte[] data) {
        return encrypt("RSA/ECB/OAEPWithSHA-1AndMGF1Padding", publicKey, data);
    }

    public static byte[] encryptNoneOaepMgf1(RSAPublicKey publicKey, byte[] data) {
        return encrypt("RSA/NONE/OAEPWithSHA-1AndMGF1Padding", publicKey, data);
    }

    public static byte[] encryptEcbOaepSha1Mgf1(RSAPublicKey publicKey, byte[] data) {
        return encrypt("RSA/ECB/OAEPWithSHA-1AndMGF1Padding", publicKey, data);
    }

    public static byte[] encryptNoneOaepSha1Mgf1(RSAPublicKey publicKey, byte[] data) {
        return encrypt("RSA/NONE/OAEPWithSHA-1AndMGF1Padding", publicKey, data);
    }

    public static byte[] encryptNonePkcs1(RSAPublicKey publicKey, byte[] data) {
        return encrypt("RSA/NONE/PKCS1Padding", publicKey, data);
    }

    private static byte[] sign(String type, PrivateKey privateKey, byte[] input) {
        try {
            Signature signature = Signature.getInstance(type);
            signature.initSign(privateKey);
            signature.update(input);
            return signature.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] signSha1(PrivateKey privateKey, byte[] input) {
        return sign("SHA1withRSA", privateKey, input);
    }

    public static byte[] signSha256(PrivateKey privateKey, byte[] input) {
        return sign("SHA256withRSA", privateKey, input);
    }

    public static String pemFormatPublicKey(PublicKey publicKey) {
        String rawPublicKey = EncodingUtils.encodeAsBase64String(publicKey.getEncoded());
        return String.format(
                "-----BEGIN PUBLIC KEY-----\n%s\n-----END PUBLIC KEY-----\n", rawPublicKey);
    }
}
