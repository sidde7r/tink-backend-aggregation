package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.authenticator;

import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioConstants;
import se.tink.libraries.cryptography.AES;
import se.tink.libraries.cryptography.KeyDerivation;
import se.tink.libraries.encoding.EncodingUtils;

public class PasswordEncryptionUtil {

    public static final int AES_IV_LENGTH = 16;

    public static String encryptPassword(String username, String password) {
        String salt = String.format(MontepioConstants.Crypto.SALT_PATTERN, username);
        byte[] saltedKey =
                KeyDerivation.pbkdf2WithHmacSha1(
                        MontepioConstants.Crypto.PASSWORD_ENCRYPTION_KEY,
                        salt.getBytes(),
                        1,
                        AES_IV_LENGTH);
        return EncodingUtils.encodeAsBase64String(
                AES.encryptCbcPkcs7(saltedKey, saltedKey, password.getBytes()));
    }
}
