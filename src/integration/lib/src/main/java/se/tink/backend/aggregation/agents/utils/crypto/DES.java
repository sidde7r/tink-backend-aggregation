package se.tink.backend.aggregation.agents.utils.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DES {
    public static byte[] encryptEcbPkcs5(byte[] key, byte[] data) {
        return ecb(true, key, data, "PKCS5Padding");
    }

    public static byte[] decryptEcbPkcs5(byte[] key, byte[] data) {
        return ecb(false, key, data, "PKCS5Padding");
    }

    public static byte[] encryptEcbNoPadding(byte[] key, byte[] data) {
        return ecb(true, key, data, "NoPadding");
    }

    public static byte[] decryptEcbNoPadding(byte[] key, byte[] data) {
        return ecb(false, key, data, "NoPadding");
    }

    public static byte[] encryptCbcPkcs5(byte[] key, byte[] iv, byte[] data) {
        return cbc(true, key, iv, data, "PKCS5Padding");
    }

    public static byte[] decryptCbcPkcs5(byte[] key, byte[] iv, byte[] data) {
        return cbc(false, key, iv, data, "PKCS5Padding");
    }

    public static byte[] encryptCbcNoPadding(byte[] key, byte[] iv, byte[] data) {
        return cbc(true, key, iv, data, "NoPadding");
    }

    public static byte[] decryptCbcNoPadding(byte[] key, byte[] iv, byte[] data) {
        return cbc(false, key, iv, data, "NoPadding");
    }

    private static byte[] ecb(boolean encrypt, byte[] key, byte[] data, String padding) {
        try {
            Cipher cipher = Cipher.getInstance(String.format("DES/ECB/%s", padding));
            SecretKey keyValue = new SecretKeySpec(key, "DES");
            int opMode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
            cipher.init(opMode, keyValue);
            return cipher.doFinal(data);
        } catch (NoSuchPaddingException
                | NoSuchAlgorithmException
                | InvalidKeyException
                | IllegalBlockSizeException
                | BadPaddingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static byte[] cbc(boolean encrypt, byte[] key, byte[] iv, byte[] data, String padding) {
        try {
            Cipher cipher = Cipher.getInstance(String.format("DES/CBC/%s", padding));
            IvParameterSpec ivValue = new IvParameterSpec(iv);
            SecretKey keyValue = new SecretKeySpec(key, "DES");
            int opMode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
            cipher.init(opMode, keyValue, ivValue);
            return cipher.doFinal(data);
        } catch (NoSuchPaddingException
                | NoSuchAlgorithmException
                | InvalidKeyException
                | IllegalBlockSizeException
                | BadPaddingException
                | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
