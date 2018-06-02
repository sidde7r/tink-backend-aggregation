package se.tink.backend.common.utils;

import java.security.interfaces.RSAPublicKey;
import javax.crypto.SecretKey;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class EncryptionUtilsTest {
    @Test
    public void testAESEncryptionAndDecryptionWithIvBasedOnSecretKey() throws Exception {
        final SecretKey key = EncryptionUtils.AES.generateSecretKey();

        String encrypted = EncryptionUtils.AES.encrypt("secret", key, EncryptionUtils.AES.IvMode.BASED_ON_SECRET_KEY);

        String decrypted = EncryptionUtils.AES.decrypt(encrypted, key, EncryptionUtils.AES.IvMode.BASED_ON_SECRET_KEY);

        assertThat(decrypted).isEqualTo("secret");
    }

    @Test
    public void testAESEncryptionAndDecryptionWithRandomIv() throws Exception {
        final SecretKey secretKey = EncryptionUtils.AES.generateSecretKey();

        String encrypted = EncryptionUtils.AES.encrypt("secret", secretKey, EncryptionUtils.AES.IvMode.RANDOM);

        String decrypted = EncryptionUtils.AES.decrypt(encrypted, secretKey, EncryptionUtils.AES.IvMode.RANDOM);

        assertThat(decrypted).isEqualTo("secret");
    }

    @Test
    public void testXmlBase64EncodedPublicRSAKey() throws Exception {
        // Must be a valid key
        String publicKey = "PFJTQUtleVZhbHVlPjxNb2R1bHVzPi9mNlp6a1Y4OG96WTBnUURrVG1ndkNGYVZseTNxZDJUTnJFMjBjTkNC"
                + "TGJUSXp2Q1ZLbloxdjRnZ0ExVEwyR3ZwNjRTcXZjNTd5bzE5aENsVUNUTjl4RndzeTdDWXZYSUpRMlk0MnduZ1dqdnd1MU"
                + "xWMzNLK0FZY2k1dUFRS2VDVXVrMWpmQTVvMUZXcUFiQXlLUmpNaDF5NjM1UEtDdFpsWXBXQy9tRFB4OD08L01vZHVsdXM+"
                + "PEV4cG9uZW50PkFRQUI8L0V4cG9uZW50PjwvUlNBS2V5VmFsdWU+";

        RSAPublicKey key = EncryptionUtils.RSA.getPublicKey(publicKey,
                EncryptionUtils.RSA.PublicKeyFormat.XML_BASE_64_ENCODED);

        assertThat(key).isNotNull();
    }

    @Test
    public void testPemBase64EncodedPublicRSAKey() throws Exception {
        // Must be a valid key
        String publicKey = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAkZDymZhzuL+WbCdhcKGZ\n"
                + "eAUiwm36gPH3JtTjqbykLuHR9G8Q9HwE7Sg/hTmTXQOKI7kU3tCStVkhyqI8BvIu\n"
                + "NrSKKowuGDiiJBh9lB86cHwwhNnsFGc5XqDgvs7MGzYePoxxdeuHGrs2eeIHHpyb\n"
                + "l8fSgl/da75Bqfaq+pNncYiJlaRJoyAaf4mzxCxFE23dCnPVFUwAF4miyA1iAP3q\n"
                + "JjgPd54hnVJ18F+dRD98kQXcVneWsHzwlWe8cbJ9Fxjf1QRZdE/o1U+sswbPaPhO\n"
                + "Vu3xRY3qFT7jlF1XfaXTW4xWybbD8vRJWqDFFJVlVDRjGZA8MAhd8bdkepnMA4/u\n"
                + "soORyp4wtPEcv6z2OX2P4++PYukmIZTOIoZpWdg+o23qPS/2ujtFbEw0ofznI3Cv\n"
                + "UAnyoaOD4M5S/rFKPDydMGm171aFLWAXPmIssJkA7/fjrBkBKZqUYRaCSBlrbzLD\n"
                + "+HSobTd/7FYMPnSNYdBxXO3SApZYyYKTJw91ctNWgWGbgQkCOS9Tp9kIvVFKpRXJ\n"
                + "/6iPI18RZePi4Xnl1YMnerg1Ytr1azSa78RD7LNvAEeUVc673gI311YHQzuLC/8Y\n"
                + "drsnxt1rOE5nfDYRfxGTsBKxmdh/CzJcHUM3b/wWT8Byba0WNheNU1Yh90bgdkw7\n"
                + "P2x1KwnF7D1ZEsjxeUyU9Y0CAwEAAQ==";

        RSAPublicKey key = EncryptionUtils.RSA.getPublicKey(publicKey,
                EncryptionUtils.RSA.PublicKeyFormat.PEM_BASE_64_ENCODED);

        assertThat(key).isNotNull();
    }
}
