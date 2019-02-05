package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.utils;

import java.nio.charset.Charset;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

public class ArgentaSecurityUtil {
    private static final Character[] HEXADECIMAL_CHARS =
            new Character[] {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
            };

    // From Android application be.argenta.bankieren.core.util.UakUtils
    public static String generatePinResponseChallenge(String challenge, String uak) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
        String concatString = challenge + uak;
        byte[] concatBytes = concatString.getBytes(Charset.defaultCharset());
        byte[] hash = messageDigest.digest(concatBytes);
        return toHexString(hash);
    }

    // From Android application  be.argenta.bankieren.core.util.KotlinExtensionsKt
    private static String toHexString(byte[] bArr) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte toHexString : bArr) {
            stringBuilder.append(toHexString(toHexString));
        }
        return stringBuilder.toString();
    }

    // From Android application  be.argenta.bankieren.core.util.KotlinExtensionsKt
    private static String toHexString(byte b) {
        char hewLow = HEXADECIMAL_CHARS[b & 15];
        char hexHigh = HEXADECIMAL_CHARS[(b >> 4) & 15];

        return String.valueOf(hexHigh) + hewLow;
    }

    // Based on Android Application
    // be.argenta.bankieren.data.securedata.old.Vault.replaceUakAndPinCode
    public static String getUak(String oldUAk, String encryptedUak, String deviceId) {
        try {
            byte[] password = Hex.decodeHex(Hex.encodeHex(removeSpecialChars(deviceId).getBytes()));
            char[] salt = oldUAk.toCharArray();

            Key secretKeySpec =
                    new SecretKeySpec(
                            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                                    .generateSecret(new PBEKeySpec(salt, password, 1024, 256))
                                    .getEncoded(),
                            "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(new byte[16]));
            return new String(cipher.doFinal(Hex.decodeHex(encryptedUak.toCharArray())));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String removeSpecialChars(String string) {
        return string != null
                ? string.replaceAll("[({]", "[")
                        .replaceAll("[)}]", "]")
                        .replaceAll("[\"*#$%&·/;<>?\\\\^`~|\"]", " ")
                : string;
    }
}
