package se.tink.backend.aggregation.agents.utils.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    public static byte[] encryptEcbPkcs5(byte[] key, byte[] data) {
        return aesEcb(true, key, data, "PKCS5Padding");
    }

    public static byte[] decryptEcbPkcs5(byte[] key, byte[] data) {
        return aesEcb(false, key, data, "PKCS5Padding");
    }

    public static byte[] encryptEcbNoPadding(byte[] key, byte[] data) {
        return aesEcb(true, key, data, "NoPadding");
    }

    public static byte[] decryptEcbNoPadding(byte[] key, byte[] data) {
        return aesEcb(false, key, data, "NoPadding");
    }

    public static byte[] encryptCbc(byte[] key, byte[] iv, byte[] data) {
        return aesCbc(true, key, iv, data, "PKCS5Padding");
    }

    public static byte[] decryptCbc(byte[] key, byte[] iv, byte[] data) {
        return aesCbc(false, key, iv, data, "PKCS5Padding");
    }

    public static byte[] decryptCbcNoPadding(byte[] key, byte[] iv, byte[] data) {
        return aesCbc(false, key, iv, data, "NoPadding");
    }

    public static byte[] encryptCbcPkcs7(byte[] key, byte[] iv, byte[] data) {
        return aesCbc(true, key, iv, data, "PKCS7Padding");
    }

    public static byte[] decryptCbcPkcs7(byte[] key, byte[] iv, byte[] data) {
        return aesCbc(false, key, iv, data, "PKCS7Padding");
    }

    public static byte[] encryptCtr(byte[] key, byte[] ctr, byte[] data) {
        return aesCtr(true, key, ctr, data);
    }

    public static byte[] decryptCtr(byte[] key, byte[] ctr, byte[] data) {
        return aesCtr(false, key, ctr, data);
    }

    public static byte[] encryptGcm(byte[] key, byte[] iv, byte[] aad, int gcmTagLength, byte[] data) {
        return aesGcm(true, key, iv, aad, gcmTagLength, data);
    }

    public static byte[] decryptGcm(byte[] key, byte[] iv, byte[] aad, int gcmTagLength, byte[] data) {
        return aesGcm(false, key, iv, aad, gcmTagLength, data);
    }

    public static byte[] encryptCfbSegmentationSize8NoPadding(byte[] key, byte[] iv, byte[] data) {
        return aesCfb(true, key, iv, data, "NoPadding");
    }

    public static byte[] decryptCfbSegmentationSize8NoPadding(byte[] key, byte[] iv, byte[] data) {
        return aesCfb(false, key, iv, data, "NoPadding");
    }

    private static byte[] aesEcb(boolean encrypt, byte[] key, byte[] data, String padding) {
        try {
            Cipher cipher = Cipher.getInstance(String.format("AES/ECB/%s", padding));
            SecretKey keyValue = new SecretKeySpec(key, "AES");
            int opMode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
            cipher.init(opMode, keyValue);
            return cipher.doFinal(data);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
                BadPaddingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static byte[] aesCbc(boolean encrypt, byte[] key, byte[] iv, byte[] data, String padding) {
        try {
            Cipher cipher = Cipher.getInstance(String.format("AES/CBC/%s", padding));
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

    private static byte[] aesCtr(boolean encrypt, byte[] key, byte[] ctr, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            IvParameterSpec ctrValue = new IvParameterSpec(ctr);
            SecretKey keyValue = new SecretKeySpec(key, "AES");
            int opMode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
            cipher.init(opMode, keyValue, ctrValue);
            return cipher.doFinal(data);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
                BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static byte[] aesGcm(boolean encrypt, byte[] key, byte[] iv, byte[] aad, int gcmTagLength, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKey keyValue = new SecretKeySpec(key, "AES");

            // configure gcm;
            //  - tag length in bits (which must be located at the end of the input data)
            //  - iv (which is 12 bytes long)
            GCMParameterSpec spec = new GCMParameterSpec(gcmTagLength * 8, Arrays.copyOfRange(iv, 0, 12));
            int opMode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
            cipher.init(opMode, keyValue, spec);
            cipher.updateAAD(aad);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static byte[] aesCfb(boolean encrypt, byte[] key, byte[] iv, byte[] data, String padding) {
        try {
            final Cipher cipher = Cipher.getInstance(String.format("AES/CFB8/%s", padding));
            final IvParameterSpec ivValue = new IvParameterSpec(iv);
            final SecretKey keyValue = new SecretKeySpec(key, "AES");
            final int opMode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
            cipher.init(opMode, keyValue, ivValue);
            return cipher.doFinal(data);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
                BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
