package se.tink.backend.common.utils;

import com.google.common.base.Charsets;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;

public class EncryptionUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static class AES {
        private static final String ALGORITHM = "AES";
        private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

        public enum IvMode {
            @Deprecated
            BASED_ON_SECRET_KEY,
            RANDOM
        }

        public static SecretKey generateSecretKey() throws Exception {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(256);

            return keyGen.generateKey();
        }

        public static String encrypt(String plainText, SecretKey secretKey, IvMode mode) throws Exception {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            switch (mode) {
            case RANDOM:
                // Generate the IV with random bytes and prepend it to the text to encrypt.
                IvParameterSpec iv = getIv();
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
                return EncryptionUtils.encrypt(iv.getIV(), getBytes(plainText), cipher);
            case BASED_ON_SECRET_KEY:
                // Generate the IV based on the secret key. Deprecated, use IvMode.RANDOM if possible.
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, getIvSpecBasedOnSecretKey(secretKey));
                return EncryptionUtils.encrypt(plainText, cipher);
            default:
                throw new NotImplementedException("IvMode not supported.");
            }
        }

        private static IvParameterSpec getIvSpecBasedOnSecretKey(SecretKey secretKey) {
            byte[] secretKeyEncoded = secretKey.getEncoded();
            byte[] ivBytes = new byte[16];

            for (int i = 0; i < 16; i++) {
                if (i < secretKeyEncoded.length) {
                    ivBytes[i] = secretKeyEncoded[i];
                } else {
                    ivBytes[i] = (byte) 0x00;
                }
            }

            return new IvParameterSpec(ivBytes);
        }

        private static IvParameterSpec getIv() {
            byte[] ivBytes = new byte[16];
            RANDOM.nextBytes(ivBytes);
            return new IvParameterSpec(ivBytes);
        }

        public static String decrypt(String encryptedText, SecretKey secretKey, IvMode mode) throws Exception {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            byte[] decodedString = decodeBase64String(encryptedText);

            IvParameterSpec iv;

            switch (mode) {
            case RANDOM:
                iv = new IvParameterSpec(Arrays.copyOfRange(decodedString, 0, 16));
                decodedString = Arrays.copyOfRange(decodedString, 16, decodedString.length);
                break;
            case BASED_ON_SECRET_KEY:
                iv = getIvSpecBasedOnSecretKey(secretKey);
                break;
            default:
                throw new NotImplementedException("IvMode not supported.");
            }

            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            return getString(cipher.doFinal(decodedString));
        }
    }

    public static class RSA {

        private static final String ALGORITHM = "RSA";
        private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
        private static final Pattern PUBLIC_KEY_XML_PATTERN = Pattern
                .compile("^<RSAKeyValue><Modulus>(?<modulus>[a-zA-Z0-9+\\/=]+)<\\/Modulus><Exponent>(?<exponent>[a-zA-Z0-9+\\/=]+)<\\/Exponent><\\/RSAKeyValue>$");

        public enum PublicKeyFormat {
            PEM_BASE_64_ENCODED,
            XML_BASE_64_ENCODED
        }

        public static RSAPublicKey getPublicKey(String publicKey, PublicKeyFormat format) throws Exception {
            switch (format) {
            case XML_BASE_64_ENCODED:
                return getPublicKeyFromBase64EncodedXML(publicKey);
            case PEM_BASE_64_ENCODED:
                return getPublicKeyFromBase64Encoded(publicKey);
            default:
                throw new NotImplementedException("Format not implemented.");
            }
        }

        private static RSAPublicKey getPublicKeyFromBase64Encoded(String publicKey) throws Exception {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKey));

            KeyFactory factory = KeyFactory.getInstance(ALGORITHM);

            return (RSAPublicKey) factory.generatePublic(keySpec);
        }

        private static RSAPublicKey getPublicKeyFromBase64EncodedXML(String publicKeyXML) throws Exception {
            return getPublicKeyFromXML(getString(decodeBase64String(publicKeyXML)));
        }

        private static RSAPublicKey getPublicKeyFromXML(String publicKeyXML) throws Exception {
            Matcher rsaKeyMatcher = PUBLIC_KEY_XML_PATTERN.matcher(publicKeyXML);

            if (!rsaKeyMatcher.find()) {
                return null;
            }

            String modulusBase64 = rsaKeyMatcher.group("modulus");
            String exponentBase64 = rsaKeyMatcher.group("exponent");

            byte[] exponentBytes = decodeBase64String(exponentBase64);
            byte[] modulusBytes = decodeBase64String(modulusBase64);

            BigInteger modulus = new BigInteger(1, modulusBytes);
            BigInteger exponent = new BigInteger(1, exponentBytes);

            KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
            RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(modulus, exponent);

            return (RSAPublicKey) factory.generatePublic(pubKeySpec);
        }

        public static String encrypt(String plainText, RSAPublicKey publicKey) throws Exception {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            
            return EncryptionUtils.encrypt(plainText, cipher);
        }
    }
    
    private static String encrypt(String plainText, Cipher cipher) throws Exception {
        byte[] encrypted = cipher.doFinal(getBytes(plainText));
        return encodeAsBase64String(encrypted);
    }

    private static String encrypt(byte[] padding, byte[] bytes, Cipher cipher) throws Exception {
        byte[] encrypted = cipher.doFinal(bytes);
        return encodeAsBase64String(ArrayUtils.addAll(padding, encrypted));
    }

    private static byte[] decodeBase64String(String base64String) {
        return getCodec().decode(base64String);
    }
    
    private static byte[] encodeAsBase64Bytes(byte[] binaryData) {
        return getCodec().encode(binaryData);
    }
    
    private static String encodeAsBase64String(byte[] binaryData) {
        return getString(encodeAsBase64Bytes(binaryData));        
    }
    
    private static String getString(byte[] binaryData) {
        return new String(binaryData, Charsets.UTF_8);
    }
    
    private static byte[] getBytes(String input) {
        return input.getBytes(Charsets.UTF_8);
    }
    
    private static Base64 getCodec() {
        return new Base64();
    }
}
