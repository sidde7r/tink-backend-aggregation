package se.tink.backend.aggregation.workers.encryption;

import static se.tink.backend.aggregation.workers.encryption.AesGcmCrypto.aesGcm;
import static se.tink.backend.aggregation.workers.encryption.AesGcmCrypto.encrypt;

import com.google.common.base.Strings;
import com.google.protobuf.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.credentials_revamp.grpc.OpaquePayload;
import se.tink.libraries.encryptedpayload.AesEncryptedData;
import se.tink.libraries.encryptedpayload.EncryptedPayloadV2;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CredentialsCryptoV2 {

    public static EncryptedPayloadV2 encryptV2(
            int keyId, byte[] key, Credentials credential) {
        EncryptedPayloadV2 encryptedCredentialsV2 = new EncryptedPayloadV2();
        encryptedCredentialsV2.setKeyId(0); // Not used in V2, fields + payload have their own keyId
        encryptedCredentialsV2.setTimestamp(new Date());

        AesEncryptedData encryptedFields =
                encrypt(
                        key,
                        credential.getFieldsSerialized(),
                        asAAD(encryptedCredentialsV2.getVersion()),
                        asAAD(keyId),
                        asAAD(encryptedCredentialsV2.getTimestamp().getTime()));

        AesEncryptedData encryptedPayload =
                encrypt(
                        key,
                        credential.getSensitivePayloadSerialized(),
                        asAAD(encryptedCredentialsV2.getVersion()),
                        asAAD(keyId),
                        asAAD(encryptedCredentialsV2.getTimestamp().getTime()));

        final OpaquePayload fields =
                OpaquePayload.newBuilder()
                        .setVersion(encryptedCredentialsV2.getVersion())
                        .setKeyId(keyId)
                        .setTimestamp(timestamp(encryptedCredentialsV2.getTimestamp()))
                        .setPayload(SerializationUtils.serializeToString(encryptedFields))
                        .build();

        final OpaquePayload payload =
                OpaquePayload.newBuilder()
                        .setVersion(encryptedCredentialsV2.getVersion())
                        .setKeyId(keyId)
                        .setTimestamp(timestamp(encryptedCredentialsV2.getTimestamp()))
                        .setPayload(SerializationUtils.serializeToString(encryptedPayload))
                        .build();

        encryptedCredentialsV2.setFields(fields);
        encryptedCredentialsV2.setPayload(payload);
        return encryptedCredentialsV2;
    }

    public static void decryptV2(
            byte[] fieldsKey,
            byte[] payloadKey,
            Credentials credential,
            EncryptedPayloadV2 encryptedCredentialsV2) {
        String decryptedFields = decryptFields(fieldsKey, encryptedCredentialsV2);
        if (!Strings.isNullOrEmpty(decryptedFields)) {
            credential.addSerializedFields(decryptedFields);
        }

        String decryptedPayload = decryptPayload(payloadKey, encryptedCredentialsV2);
        if (!Strings.isNullOrEmpty(decryptedPayload)) {
            credential.setSensitivePayloadSerialized(decryptedPayload);
        }
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

    public static Timestamp timestamp(Date date) {
        Instant instant = date.toInstant();
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    public static long timestamp(Timestamp timestamp) {
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
