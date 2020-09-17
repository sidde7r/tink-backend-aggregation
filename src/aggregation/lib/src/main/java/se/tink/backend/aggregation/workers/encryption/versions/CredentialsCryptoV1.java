package se.tink.backend.aggregation.workers.encryption.versions;

import static se.tink.backend.aggregation.workers.encryption.versions.AesGcmCrypto.aesGcm;
import static se.tink.backend.aggregation.workers.encryption.versions.AesGcmCrypto.encrypt;

import com.google.common.base.Strings;
import java.util.Date;
import java.util.Objects;
import se.tink.libraries.encryptedpayload.AesEncryptedData;
import se.tink.libraries.encryptedpayload.EncryptedPayloadV1;

public class CredentialsCryptoV1 {

    public static class DecryptedDataV1 {
        public String decryptedFields;
        public String decryptedPayload;
    }

    public static EncryptedPayloadV1 encryptV1(
            int keyId, byte[] key, String serializedFields, String serializedSensitivePayload) {
        EncryptedPayloadV1 encryptedPayloadV1 = new EncryptedPayloadV1();
        encryptedPayloadV1.setKeyId(keyId);
        encryptedPayloadV1.setTimestamp(new Date());

        AesEncryptedData encryptedFields =
                encrypt(
                        key,
                        serializedFields,
                        encryptedPayloadV1.getVersionAsAAD(),
                        encryptedPayloadV1.getKeyIdAsAAD(),
                        encryptedPayloadV1.getTimestampAsAAD());

        AesEncryptedData encryptedPayload =
                encrypt(
                        key,
                        serializedSensitivePayload,
                        encryptedPayloadV1.getVersionAsAAD(),
                        encryptedPayloadV1.getKeyIdAsAAD(),
                        encryptedPayloadV1.getTimestampAsAAD());

        encryptedPayloadV1.setFields(encryptedFields);
        encryptedPayloadV1.setPayload(encryptedPayload);
        return encryptedPayloadV1;
    }

    public static DecryptedDataV1 decryptV1(byte[] key, EncryptedPayloadV1 encryptedPayloadV1) {
        DecryptedDataV1 result = new DecryptedDataV1();

        String decryptedFields = CredentialsCryptoV1.decryptFields(key, encryptedPayloadV1);
        if (!Strings.isNullOrEmpty(decryptedFields)) {
            result.decryptedFields = decryptedFields;
        }

        String decryptedPayload = CredentialsCryptoV1.decryptPayload(key, encryptedPayloadV1);
        if (!Strings.isNullOrEmpty(decryptedPayload)) {
            result.decryptedPayload = decryptedPayload;
        }

        return result;
    }

    private static String decryptFields(byte[] key, EncryptedPayloadV1 encryptedCredentialsV1) {
        return decryptAesEncryptedData(
                key, encryptedCredentialsV1, encryptedCredentialsV1.getFields());
    }

    private static String decryptPayload(byte[] key, EncryptedPayloadV1 encryptedCredentialsV1) {
        return decryptAesEncryptedData(
                key, encryptedCredentialsV1, encryptedCredentialsV1.getPayload());
    }

    private static String decryptAesEncryptedData(
            byte[] key, EncryptedPayloadV1 encryptedCredentialsV1, AesEncryptedData encryptedData) {

        if (Objects.isNull(encryptedData.getData()) && Objects.isNull(encryptedData.getIv())) {
            // An empty data object means that it had nothing to encrypt. (see encrypt())
            return "";
        }

        byte[] decryptedData =
                aesGcm(
                        false,
                        key,
                        encryptedData.getIv(),
                        encryptedData.getData(),
                        encryptedCredentialsV1.getVersionAsAAD(),
                        encryptedCredentialsV1.getKeyIdAsAAD(),
                        encryptedCredentialsV1.getTimestampAsAAD());

        return new String(decryptedData);
    }
}
