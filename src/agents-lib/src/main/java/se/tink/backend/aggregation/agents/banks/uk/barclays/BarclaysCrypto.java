package se.tink.backend.aggregation.agents.banks.uk.barclays;

import com.fasterxml.jackson.core.type.TypeReference;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BarclaysCrypto {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int AES_GCM_TAG_LENGTH = 16;

    public static int getAesGcmTagLength() {
        return AES_GCM_TAG_LENGTH;
    }

    private static byte[] aesEcb(boolean encrypt, byte[] key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKey keyValue = new SecretKeySpec(key, "AES");
            int opMode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
            cipher.init(opMode, keyValue);
            return cipher.doFinal(data);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
                BadPaddingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] aesEcbEncrypt(byte[] key, byte[] data) {
        return aesEcb(true, key, data);
    }

    public static byte[] aesEcbDecrypt(byte[] key, byte[] data) {
        return aesEcb(false, key, data);
    }

    private static byte[] aesCbc(boolean encrypt, byte[] key, byte[] iv, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivValue = new IvParameterSpec(iv);
            SecretKey keyValue = new SecretKeySpec(key, "AES");
            int opMode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
            cipher.init(opMode, keyValue, ivValue);
            return cipher.doFinal(data);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
                BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static byte[] aesGcm(boolean encrypt, byte[] key, byte[] iv, byte[] aad, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKey keyValue = new SecretKeySpec(key, "AES");

            // configure gcm;
            //  - tag length in bits (which must be located at the end of the input data)
            //  - iv (which is 12 bytes long)
            GCMParameterSpec spec = new GCMParameterSpec(AES_GCM_TAG_LENGTH * 8, Arrays.copyOfRange(iv, 0, 12));
            int opMode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
            cipher.init(opMode, keyValue, spec);
            cipher.updateAAD(aad);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] aesGcmEncrypt(byte[] key, byte[] iv, byte[] aad, byte[] data) {
        return aesGcm(true, key, iv, aad, data);
    }

    public static byte[] aesGcmDecrypt(byte[] key, byte[] iv, byte[] aad, byte[] data) {
        return aesGcm(false, key, iv, aad, data);
    }

    private static byte[] hmacSha256(byte[] key, byte[] data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keyValue = new SecretKeySpec(key, "HmacSHA256");
            hmac.init(keyValue);
            return hmac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] random(int len) {
        byte[] rdata = new byte[len];
        RANDOM.nextBytes(rdata);
        return rdata;
    }

    public static byte[] sha256(final byte[]... datas) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for (byte[] data : datas) {
                md.update(data);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] rsaOaepEncrypt(byte[] pubKeyBytes, byte[] data) {
        try {
            PublicKey pubKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubKeyBytes));
            // Note the usage of SHA256 as masking generating function (mgf1). Default is sha1.
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "BC");
            OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                                                    "SHA-256",
                                                    "MGF1",
                                                    MGF1ParameterSpec.SHA256,
                                                    PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.ENCRYPT_MODE, pubKey, oaepParams);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] rsaOaepDecrypt(byte[] privKeyBytes, byte[] data) {
        try {
            PrivateKey privKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privKeyBytes));
            // Note the usage of SHA256 as masking generating function (mgf1). Default is sha1.
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "BC");
            OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                    "SHA-256",
                    "MGF1",
                    MGF1ParameterSpec.SHA256,
                    PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, privKey, oaepParams);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static KeyPair ecGenerateKeyPair() {
        try {
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("prime256v1");
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDSA", "BC");
            kpg.initialize(ecSpec, RANDOM);
            return kpg.generateKeyPair();
        } catch(NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] ecSignSha256(KeyPair key, byte[] data) {
        try {
            Signature dsa = Signature.getInstance("SHA256withECDSA");
            dsa.initSign(key.getPrivate());
            dsa.update(data);
            return dsa.sign();
        } catch(NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static PublicKey deserializePublicKey(String algorithm, byte[] keyData) {
        try {
            KeyFactory kf = KeyFactory.getInstance(algorithm);
            X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(keyData);
            return kf.generatePublic(pkSpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static PrivateKey deserializePrivateKey(String algorithm, byte[] keyData) {
        try {
            KeyFactory kf = KeyFactory.getInstance(algorithm);
            PKCS8EncodedKeySpec pkSpec = new PKCS8EncodedKeySpec(keyData);
            return kf.generatePrivate(pkSpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static String serializeKeyPair(KeyPair kp) {
        PublicKey pubKey = kp.getPublic();
        PrivateKey privKey = kp.getPrivate();

        Map<String,String> m = new HashMap<String,String>();
        m.put("alg", privKey.getAlgorithm());
        m.put("pubKey", Hex.encodeHexString(pubKey.getEncoded()));
        m.put("privKey", Hex.encodeHexString(privKey.getEncoded()));
        return SerializationUtils.serializeToString(m);
    }

    public static KeyPair deserializeKeyPair(String data) {
        try {
            HashMap<String, String> m = SerializationUtils.deserializeFromString(
                                                                data,
                                                                new TypeReference<HashMap<String, String>>() { });
            byte[] pubKeyBytes = Hex.decodeHex(m.get("pubKey").toCharArray());
            byte[] privKeyBytes = Hex.decodeHex(m.get("privKey").toCharArray());

            PublicKey pubKey = deserializePublicKey(m.get("alg"), pubKeyBytes);
            PrivateKey privKey = deserializePrivateKey(m.get("alg"), privKeyBytes);

            return new KeyPair(pubKey, privKey);
        } catch (DecoderException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] ecdhDerive(KeyPair privKey, byte[] pubKeyBytes) {
        try {
            PublicKey pubKey = deserializePublicKey("ECDSA", pubKeyBytes);
            KeyAgreement ka = KeyAgreement.getInstance("ECDH");
            ka.init(privKey.getPrivate());
            ka.doPhase(pubKey, true);
            return ka.generateSecret();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
