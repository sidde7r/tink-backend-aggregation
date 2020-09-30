package se.tink.backend.aggregation.workers.encryption.versions;

import static se.tink.backend.aggregation.workers.encryption.versions.AesGcmCrypto.aesGcm;
import static se.tink.backend.aggregation.workers.encryption.versions.AesGcmCrypto.encrypt;

import com.google.common.base.Strings;
import com.google.protobuf.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import se.tink.backend.credentials_revamp.grpc.OpaquePayload;
import se.tink.libraries.encryptedpayload.AesEncryptedData;
import se.tink.libraries.encryptedpayload.EncryptedPayloadV2;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CredentialsCryptoV2 {

    public static class DecryptedDataV2 {
        private String decryptedFields;
        private String decryptedPayload;

        public String getDecryptedFields() {
            return decryptedFields;
        }

        public String getDecryptedPayload() {
            return decryptedPayload;
        }
    }

    public static EncryptedPayloadV2 encryptV2(
            int keyId, byte[] key, String serializedFields, String serializedSensitivePayload) {
        EncryptedPayloadV2 encryptedPayloadV2 = new EncryptedPayloadV2();
        encryptedPayloadV2.setKeyId(0); // Not used in V2, fields + payload have their own keyId
        encryptedPayloadV2.setTimestamp(new Date());

        AesEncryptedData encryptedFields =
                encrypt(
                        key,
                        serializedFields,
                        asAAD(encryptedPayloadV2.getVersion()),
                        asAAD(keyId),
                        asAAD(encryptedPayloadV2.getTimestamp().getTime()));

        AesEncryptedData encryptedPayload =
                encrypt(
                        key,
                        serializedSensitivePayload,
                        asAAD(encryptedPayloadV2.getVersion()),
                        asAAD(keyId),
                        asAAD(encryptedPayloadV2.getTimestamp().getTime()));

        final OpaquePayload fields =
                OpaquePayload.newBuilder()
                        .setVersion(encryptedPayloadV2.getVersion())
                        .setKeyId(keyId)
                        .setTimestamp(timestamp(encryptedPayloadV2.getTimestamp()))
                        .setPayload(SerializationUtils.serializeToString(encryptedFields))
                        .build();

        final OpaquePayload payload =
                OpaquePayload.newBuilder()
                        .setVersion(encryptedPayloadV2.getVersion())
                        .setKeyId(keyId)
                        .setTimestamp(timestamp(encryptedPayloadV2.getTimestamp()))
                        .setPayload(SerializationUtils.serializeToString(encryptedPayload))
                        .build();

        encryptedPayloadV2.setFields(fields);
        encryptedPayloadV2.setPayload(payload);
        return encryptedPayloadV2;
    }

    public static DecryptedDataV2 decryptV2(
            byte[] fieldsKey, byte[] payloadKey, EncryptedPayloadV2 encryptedPayloadV2) {
        DecryptedDataV2 result = new DecryptedDataV2();

        String decryptedFields = decryptFields(fieldsKey, encryptedPayloadV2);
        if (!Strings.isNullOrEmpty(decryptedFields)) {
            result.decryptedFields = decryptedFields;
        }

        if (payloadKey != null && encryptedPayloadV2.getPayload() != null) {
            String decryptedPayload = decryptPayload(payloadKey, encryptedPayloadV2);
            if (!Strings.isNullOrEmpty(decryptedPayload)) {
                result.decryptedPayload = decryptedPayload;
            }
        }
        return result;
    }

    private static String decryptFields(byte[] key, EncryptedPayloadV2 encryptedCredentialsV2) {
        return decryptAesEncryptedData(key, encryptedCredentialsV2.getFields());
    }

    private static String decryptPayload(byte[] key, EncryptedPayloadV2 encryptedCredentialsV2) {
        return decryptAesEncryptedData(key, encryptedCredentialsV2.getPayload());
    }

    private static String decryptAesEncryptedData(byte[] key, OpaquePayload opaquePayload) {

        final AesEncryptedData encryptedData =
                SerializationUtils.deserializeFromString(
                        opaquePayload.getPayload(), AesEncryptedData.class);

        if (encryptedData == null) {
            // An empty data object means that it had nothing to encrypt. (see encrypt())
            return "";
        }

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
                        asAAD(opaquePayload.getVersion()),
                        asAAD(opaquePayload.getKeyId()),
                        asAAD(timestamp(opaquePayload.getTimestamp())));

        return new String(decryptedData);
    }

    private static Timestamp timestamp(Date date) {
        Instant instant = date.toInstant();
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    private static long timestamp(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()).toEpochMilli();
    }

    private static byte[] asAAD(long input) {
        return asAAD(Long.toString(input));
    }

    private static byte[] asAAD(int input) {
        return asAAD(Integer.toString(input));
    }

    private static byte[] asAAD(String input) {
        return input.getBytes();
    }
}
