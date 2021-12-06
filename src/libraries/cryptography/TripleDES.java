package se.tink.libraries.cryptography;

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

public class TripleDES {
    private static final String NO_PADDING = "NoPadding";

    public static byte[] encryptEcbNoPadding(byte[] key, byte[] data) {
        return ecb(true, key, data, NO_PADDING);
    }

    public static byte[] encryptCbcNoPadding(byte[] key, byte[] iv, byte[] data) {
        return cbc(true, key, iv, data, NO_PADDING);
    }

    public static byte[] decryptCbcNoPadding(byte[] key, byte[] iv, byte[] data) {
        return cbc(false, key, iv, data, NO_PADDING);
    }

    private static byte[] ecb(boolean encrypt, byte[] key, byte[] data, String padding) {
        try {
            Cipher cipher = Cipher.getInstance(String.format("DESede/ECB/%s", padding));
            SecretKey keyValue = new SecretKeySpec(key, "DESede");
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
            Cipher cipher = Cipher.getInstance(String.format("DESede/CBC/%s", padding));
            IvParameterSpec ivValue = new IvParameterSpec(iv);
            SecretKey keyValue = new SecretKeySpec(key, "DESede");
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
