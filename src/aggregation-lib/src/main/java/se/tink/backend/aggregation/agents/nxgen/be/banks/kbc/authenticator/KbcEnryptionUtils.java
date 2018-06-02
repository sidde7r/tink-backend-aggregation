package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator;

import java.util.Arrays;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.ActivationLicenseResponse;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.KeyDerivation;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class KbcEnryptionUtils {

    static byte[] deriveKey(String activationPassword) {
        return KeyDerivation.pbkdf2WithHmacSha256(
                activationPassword,
                KbcConstants.Encryption.SALT,
                KbcConstants.Encryption.ITERATIONS,
                KbcConstants.Encryption.AES_KEY_LENGTH);
    }

    static byte[] decryptServerPublicKey(byte[] aesKey0, ActivationLicenseResponse activationLicenseResponse) {
        byte[] initialVectorSession =
                EncodingUtils.decodeHexString(activationLicenseResponse.getInitialVectorSession().getValue());
        byte[] encryptedServerPublicKey =
                EncodingUtils.decodeHexString(activationLicenseResponse.getEncryptedServerPublicKey().getValue());

        return AES.decryptCfbSegmentationSize8NoPadding(aesKey0, initialVectorSession, encryptedServerPublicKey);
    }

    static byte[] decryptStaticVector(byte[] sharedSecret,
            ActivationLicenseResponse activationLicenseResponse) {
        byte[] initialVectorStaticVector =
                EncodingUtils.decodeHexString(activationLicenseResponse.getInitialVectorStaticVector().getValue());
        byte[] encryptedStaticVector =
                EncodingUtils.decodeHexString(activationLicenseResponse.getStaticVector().getValue());

        return AES.decryptCbc(sharedSecret, initialVectorStaticVector, encryptedStaticVector);
    }

    static byte[] decryptDynamicVector(byte[] sharedSecret,
            ActivationLicenseResponse activationLicenseResponse) {
        byte[] initialVectorData =
                EncodingUtils.decodeHexString(activationLicenseResponse.getInitialVectorData().getValue());
        byte[] encryptedData =
                EncodingUtils.decodeHexString(activationLicenseResponse.getData().getValue());

        return AES.decryptCbc(sharedSecret, initialVectorData, encryptedData);
    }

    static String decryptAndEncryptNonce(byte[] sharedSecret, byte[] iv,
            ActivationLicenseResponse activationLicenseResponse) {
        byte[] initialVectorSession =
                EncodingUtils.decodeHexString(activationLicenseResponse.getInitialVectorSession().getValue());
        byte[] encryptedServerNonces =
                EncodingUtils.decodeHexString(activationLicenseResponse.getEncryptedServerNonces().getValue());

        byte[] serverNonces = AES.decryptCbc(sharedSecret, initialVectorSession, encryptedServerNonces);
        byte[] encryptedNonce = AES.encryptCbc(sharedSecret, iv, Arrays.copyOfRange(serverNonces, 0, 4));

        return EncodingUtils.encodeHexAsString(encryptedNonce).toUpperCase();
    }
}
