package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper;

import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisProcessState;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.KeyDerivation;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class FortisKeyDecryptor {

    private static final int DATA_LENGTH = 64;
    private static final int SIGNATURE_LENGTH = 32;
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 16;
    private static final int KEY_LENGTH = 16;
    private static final int GARBAGE_OFFSET = 5;

    public static byte[] decryptKey(FortisProcessState processState) {
        byte[] credentialsWithSignature =
                EncodingUtils.decodeBase64String(processState.getEncCredentials());

        byte[] encryptedCredentials = new byte[DATA_LENGTH + SIGNATURE_LENGTH];
        System.arraycopy(
                credentialsWithSignature,
                0,
                encryptedCredentials,
                0,
                DATA_LENGTH + SIGNATURE_LENGTH);

        byte[] iv = new byte[IV_LENGTH]; // 0 initialised vector

        byte[] decryptedCredentials =
                AES.decryptCbcNoPadding(
                        EncodingUtils.decodeHexString(processState.getEncryptionKey()),
                        iv,
                        encryptedCredentials);

        byte[] salt = new byte[SALT_LENGTH];
        System.arraycopy(decryptedCredentials, GARBAGE_OFFSET, salt, 0, SALT_LENGTH);
        byte[] data = new byte[DATA_LENGTH];
        System.arraycopy(decryptedCredentials, GARBAGE_OFFSET + SALT_LENGTH, data, 0, DATA_LENGTH);

        byte[] derivedKey =
                KeyDerivation.pbkdf2WithHmacSha256(processState.getSmsOtp(), salt, 3, KEY_LENGTH);

        return AES.decryptCbcNoPadding(derivedKey, iv, data);
    }
}
