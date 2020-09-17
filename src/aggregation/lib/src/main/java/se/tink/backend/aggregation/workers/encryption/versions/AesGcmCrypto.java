package se.tink.backend.aggregation.workers.encryption.versions;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import se.tink.libraries.encryptedpayload.AesEncryptedData;

public class AesGcmCrypto {
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int KEY_LENGTH = 32;
    private static final int IV_LENGTH =
            12; // 12 is the standard length. Any longer and it will do internal hashing.
    private static final int GCM_TAG_LENGTH = 16 * 8; // in bits

    private static byte[] generateIv() {
        byte[] output = new byte[IV_LENGTH];
        RANDOM.nextBytes(output);
        return output;
    }

    public static AesEncryptedData encrypt(byte[] key, String data, byte[]... aads) {
        if (Strings.isNullOrEmpty(data)) {
            // Do not encrypt empty data. Just return an empty object.
            return new AesEncryptedData();
        }
        byte[] iv = generateIv();
        byte[] encryptedData = aesGcm(true, key, iv, data.getBytes(), aads);
        return new AesEncryptedData().setIv(iv).setData(encryptedData);
    }

    public static byte[] aesGcm(
            boolean encrypt, byte[] key, byte[] iv, byte[] data, byte[]... aads) {
        Preconditions.checkArgument(
                key.length == KEY_LENGTH,
                "Invalid key length. The expected length is %d but input was of length %d.",
                KEY_LENGTH,
                key.length);

        Preconditions.checkArgument(
                iv.length == IV_LENGTH,
                "Invalid IV length. The expected length is %d but input was of length %d.",
                IV_LENGTH,
                iv.length);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKey keyValue = new SecretKeySpec(key, "AES");

            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            int opMode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
            cipher.init(opMode, keyValue, spec);

            // Include IV in the authentication tag
            cipher.updateAAD(iv);

            for (byte[] aad : aads) {
                cipher.updateAAD(aad);
            }
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException
                | InvalidAlgorithmParameterException
                | IllegalBlockSizeException
                | InvalidKeyException
                | NoSuchPaddingException
                | BadPaddingException e) {
            // We will end up here if someone has tampered with the input data, i.e. the key is
            // wrong or the AAD.
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
