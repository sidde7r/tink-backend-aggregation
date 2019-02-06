package se.tink.backend.aggregation.agents.utils.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class KeyDerivation {
    private static byte[] pbkdf2(String password, byte[] salt, int iterations, int outputLength, String algorithm) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, outputLength * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] pbkdf2WithHmacSha256(String password, byte[] salt, int iterations, int outputLength) {
        return pbkdf2(password, salt, iterations, outputLength, "PBKDF2WithHmacSHA256");
    }

    public static byte[] pbkdf2WithHmacSha1(String password, byte[] salt, int iterations, int outputLength) {
        return pbkdf2(password, salt, iterations, outputLength, "PBKDF2WithHmacSHA1");
    }
}
