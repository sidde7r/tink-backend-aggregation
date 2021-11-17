package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.DeviceActivationRequest;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.KeyDerivation;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.libraries.cryptography.hash.Hash;
import se.tink.libraries.encoding.EncodingUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BbvaMxUtils {

    public static String generateDeviceId() {
        return RandomStringUtils.randomAlphanumeric(32).toUpperCase();
    }

    public static String getUseragent() {
        return BbvaMxConstants.VALUES.USER_AGENT_VALUE;
    }

    public static String generateBoundary() {
        return String.format("--%s", UUID.randomUUID().toString());
    }

    public static String getActivationDataBoundary(
            DeviceActivationRequest request, String boundary) {
        StringBuilder builder = new StringBuilder();
        builder.append(boundary).append(BbvaMxConstants.NEW_LINE);
        builder.append(BbvaMxConstants.VALUES.CONTENT_DISPOSITION_DATA)
                .append(BbvaMxConstants.NEW_LINE)
                .append(BbvaMxConstants.NEW_LINE);
        builder.append(SerializationUtils.serializeToString(request))
                .append(BbvaMxConstants.NEW_LINE);
        builder.append(boundary).append(BbvaMxConstants.NEW_LINE);
        builder.append(BbvaMxConstants.VALUES.CONTENT_DISPOSITION_BIOMETRIC)
                .append(BbvaMxConstants.NEW_LINE)
                .append(BbvaMxConstants.NEW_LINE)
                .append(BbvaMxConstants.NEW_LINE);
        builder.append(boundary).append(BbvaMxConstants.DELIMITER);
        return builder.toString();
    }

    public static TypeMapper<AccountTypes> getTypeMapper(Map<AccountTypes, List<String>> map) {
        return TypeMapper.<AccountTypes>builder().putAll(map).build();
    }

    public static String generateTokenHash(String softwareTokenAuthCode) {

        byte[] codeHash = Hash.sha256(softwareTokenAuthCode);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeHash) + "=";
    }

    public static String generateSalt() {
        return RandomUtils.generateRandomBase64UrlEncoded(8);
    }

    public static String calculateSoftwareAuthCodeHash(String softwareTokenAuthCode) {
        return Hash.sha256AsHex(softwareTokenAuthCode);
    }

    public static String calculateData(
            String tokenHash,
            String applicationCode,
            String applicationVersion,
            String publicKeyHex) {
        StringBuilder builder = new StringBuilder();

        builder.append(tokenHash)
                .append("#")
                .append(applicationCode)
                .append("#")
                .append(applicationVersion)
                .append("#")
                .append(publicKeyHex);

        return Hash.sha256AsHex(builder.toString());
    }

    public static String generateAuthenticationCode(
            String softwareTokenAuthCode,
            String tokenHash,
            String applicationCode,
            String applicationCodeVersion,
            String salt,
            String publicKeyHex) {

        byte[] base64DecodedSalt = Base64.getUrlDecoder().decode(salt);

        String calculatedData =
                calculateData(tokenHash, applicationCode, applicationCodeVersion, publicKeyHex);

        String aesData =
                getAuthenticationCodePlaintext(
                        tokenHash,
                        applicationCode,
                        applicationCodeVersion,
                        calculatedData,
                        publicKeyHex);
        byte[] aesKey = getAESKey(softwareTokenAuthCode, base64DecodedSalt);
        byte[] result = cbcEncrypt(aesKey, aesData.getBytes());

        return EncodingUtils.encodeAsBase64String(result);
    }

    public static String getAuthenticationCodePlaintext(
            String tokenHash,
            String applicationCode,
            String applicationCodeVersion,
            String calculatedData,
            String publicKeyHex) {
        StringBuilder builder = new StringBuilder();

        builder.append(tokenHash)
                .append("#")
                .append(applicationCode)
                .append("#")
                .append(applicationCodeVersion)
                .append("#")
                .append(publicKeyHex)
                .append("#")
                .append(calculatedData);

        return builder.toString();
    }

    public static byte[] getAESKey(
            String softwareTokenAuthCodeInPlainText, byte[] base64DecodedSalt) {
        byte[] res =
                KeyDerivation.pbkdf2WithHmacSha1(
                        softwareTokenAuthCodeInPlainText, base64DecodedSalt, 1000, 32);
        return res;
    }

    public static byte[] cbcEncrypt(byte[] key, byte[] data) {
        return AES.encryptCbc(key, new byte[16], data);
    }
}
