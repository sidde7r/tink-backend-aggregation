package se.tink.backend.aggregation.workers.encryption;

import static se.tink.backend.aggregation.workers.encryption.AesGcmCrypto.aesGcm;
import static se.tink.backend.aggregation.workers.encryption.AesGcmCrypto.encrypt;

import com.google.common.base.Strings;
import java.util.Date;
import java.util.Objects;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.encryptedpayload.AesEncryptedData;
import se.tink.libraries.encryptedpayload.EncryptedPayloadV1;

public class CredentialsCryptoV1 {

    public static EncryptedPayloadV1 encryptV1(
            int keyId, byte[] key, String serializedFields, String serializedSensitivePayload) {
        EncryptedPayloadV1 encryptedCredentialsV1 = new EncryptedPayloadV1();
        encryptedCredentialsV1.setKeyId(keyId);
        encryptedCredentialsV1.setTimestamp(new Date());

        AesEncryptedData encryptedFields =
                encrypt(
                        key,
                        serializedFields,
                        encryptedCredentialsV1.getVersionAsAAD(),
                        encryptedCredentialsV1.getKeyIdAsAAD(),
                        encryptedCredentialsV1.getTimestampAsAAD());

        AesEncryptedData encryptedPayload =
                encrypt(
                        key,
                        serializedSensitivePayload,
                        encryptedCredentialsV1.getVersionAsAAD(),
                        encryptedCredentialsV1.getKeyIdAsAAD(),
                        encryptedCredentialsV1.getTimestampAsAAD());

        encryptedCredentialsV1.setFields(encryptedFields);
        encryptedCredentialsV1.setPayload(encryptedPayload);
        return encryptedCredentialsV1;
    }

    public static void decryptCredential(
            byte[] key, Credentials credential, EncryptedPayloadV1 encryptedCredentialsV1) {
        String decryptedFields = CredentialsCryptoV1.decryptFields(key, encryptedCredentialsV1);
        if (!Strings.isNullOrEmpty(decryptedFields)) {
            credential.addSerializedFields(decryptedFields);
        }

        String decryptedPayload = CredentialsCryptoV1.decryptPayload(key, encryptedCredentialsV1);
        if (!Strings.isNullOrEmpty(decryptedPayload)) {
            credential.setSensitivePayloadSerialized(decryptedPayload);
        }
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
