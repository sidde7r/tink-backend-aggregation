package se.tink.backend.aggregation.agents.utils.authentication.encap;

import com.google.common.collect.Maps;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.encrypted.RequestBody;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class EncryptedMessageService {

    private static void setDeviceInformation(Map<String, String> queryPairs, Map<String, String> encapStorage) {
        queryPairs.put("device.ApplicationHash", encapStorage.get(EncapConstants.Storage.B64_APPLICATION_HASH));
        queryPairs.put("device.DeviceHash", encapStorage.get(EncapConstants.Storage.B64_DEVICE_HASH));
        queryPairs.put("device.DeviceManufacturer", EncapConstants.DeviceInformation.MANUFACTURER);
        queryPairs.put("device.DeviceModel", EncapConstants.DeviceInformation.MODEL);
        queryPairs.put("device.DeviceName", EncapConstants.DeviceInformation.NAME);
        queryPairs.put("device.DeviceUUID", encapStorage.get(EncapConstants.Storage.DEVICE_UUID));
        queryPairs.put("device.IsRootAvailable", EncapConstants.DeviceInformation.IS_ROOT_AVAILABLE);
        queryPairs.put("device.OperatingSystemName", EncapConstants.DeviceInformation.OS_NAME_AND_TYPE);
        queryPairs.put("device.OperatingSystemType", EncapConstants.DeviceInformation.OS_NAME_AND_TYPE);
        queryPairs.put("device.SignerHashes", EncapConstants.DeviceInformation.SIGNER_HASHES);
        queryPairs.put("device.SystemVersion", EncapConstants.DeviceInformation.SYSTEM_VERSION);
        queryPairs.put("device.UserInterfaceIdiom", EncapConstants.DeviceInformation.USER_INTERFACE_IDIOM);
    }

    static String buildFirstMessageForActivation(Map<String, String> encapStorage) {
        Map<String, String> queryPairs = Maps.newLinkedHashMap();

        queryPairs.put("applicationId", EncapConstants.MessageInformation.APPLICATION_ID);
        setDeviceInformation(queryPairs, encapStorage);
        queryPairs.put("hexAPNToken", EncapConstants.MessageInformation.HEX_APN_TOKEN);
        EncapUtils.setMetaInformation(queryPairs, encapStorage);
        queryPairs.put("operation", EncapConstants.MessageInformation.OPERATION_REGISTER);
        queryPairs.put("response.requireToken", EncapConstants.MessageInformation.REQUIRE_TOKEN);

        return EncapUtils.getUrlEncodedQueryParams(queryPairs);
    }

    static String buildFirstMessageForAuthentication(Map<String, String> encapStorage) {
        Map<String, String> queryPairs = Maps.newLinkedHashMap();

        queryPairs.put("applicationId", EncapConstants.MessageInformation.APPLICATION_ID);
        queryPairs.put("clientOnly", EncapConstants.MessageInformation.CLIENT_ONLY);
        queryPairs.put("clientSaltCurrentKeyId", encapStorage.get(EncapConstants.Storage.CLIENT_SALT_CURRENT_KEY_ID));
        setDeviceInformation(queryPairs, encapStorage);
        EncapUtils.setMetaInformation(queryPairs, encapStorage);
        queryPairs.put("operation", EncapConstants.MessageInformation.OPERATION_IDENTIFY);
        queryPairs.put("purpose", EncapConstants.MessageInformation.PURPOSE);
        queryPairs.put("registrationId", encapStorage.get(EncapConstants.Storage.REGISTRATION_ID));
        queryPairs.put("response.requireToken", EncapConstants.MessageInformation.REQUIRE_TOKEN);

        return EncapUtils.getUrlEncodedQueryParams(queryPairs);
    }

    static String buildSecondMessageForActivation(Map<String, String> encapStorage) {
        Map<String, String> queryPairs = Maps.newLinkedHashMap();

        queryPairs.put("activatedAuthMethods", EncapConstants.MessageInformation.DEVICE_PIN);
        queryPairs.put("applicationId", EncapConstants.MessageInformation.APPLICATION_ID);
        queryPairs.put("b64AuthenticationKey", encapStorage.get(EncapConstants.Storage.B64_AUTHENTICATION_KEY));
        queryPairs.put("b64AuthenticationKeyWithoutPin",
                encapStorage.get(EncapConstants.Storage.B64_AUTHENTICATION_KEY_WITHOUT_PIN));
        queryPairs.put("b64ChallengeResponse", encapStorage.get(EncapConstants.Storage.B64_CHALLENGE_RESPONSE));
        queryPairs.put("b64ChallengeResponseWithoutPin",
                encapStorage.get(EncapConstants.Storage.B64_CHALLENGE_RESPONSE_WITHOUT_PIN));
        queryPairs.put("b64TotpKey", encapStorage.get(EncapConstants.Storage.B64_TOTP_KEY));
        setDeviceInformation(queryPairs, encapStorage);
        EncapUtils.setMetaInformation(queryPairs, encapStorage);
        queryPairs.put("operation", EncapConstants.MessageInformation.OPERATION_ACTIVATE);
        queryPairs.put("registrationId", encapStorage.get(EncapConstants.Storage.REGISTRATION_ID));
        queryPairs.put("saltHash", encapStorage.get(EncapConstants.Storage.B64_SALT_HASH));

        // Prepending string because the queryPairs map only handles unique keys
        return "activatedAuthMethods=DEVICE&" + EncapUtils.getUrlEncodedQueryParams(queryPairs);
    }

    static String buildSecondMessageForAuthentication(Map<String, String> encapStorage) {
        Map<String, String> queryPairs = Maps.newLinkedHashMap();

        queryPairs.put("applicationId", EncapConstants.MessageInformation.APPLICATION_ID);
        queryPairs.put("b64ResponseCurrent", encapStorage.get(EncapConstants.Storage.B64_RESPONSE_CURRENT));
        setDeviceInformation(queryPairs, encapStorage);
        queryPairs.put("hexAPNToken", EncapConstants.MessageInformation.HEX_APN_TOKEN);
        EncapUtils.setMetaInformation(queryPairs, encapStorage);
        queryPairs.put("operation", EncapConstants.MessageInformation.OPERATION_AUTHENTICATE);
        queryPairs.put("registrationId", encapStorage.get(EncapConstants.Storage.REGISTRATION_ID));
        queryPairs.put("saltHash1", encapStorage.get(EncapConstants.Storage.B64_SALT_HASH));
        queryPairs.put("usedAuthMethod", EncapConstants.MessageInformation.DEVICE_PIN);

        return EncapUtils.getUrlEncodedQueryParams(queryPairs);
    }

    static private HashMap<String, String> getCryptoRequestParams(byte[] rand16BytesKey, byte[] rand16BytesIv,
            byte[] serverPubKeyBytes, String inputInPlainText) {
        HashMap<String, String> requestParams = Maps.newHashMap();
        requestParams.put("EMD", EncapCrypto.computeEMD(rand16BytesKey, rand16BytesIv, inputInPlainText.getBytes()));
        requestParams.put("EMK", EncapCrypto.computeEMK(rand16BytesKey, rand16BytesIv, serverPubKeyBytes));
        requestParams.put("MAC", EncapCrypto.computeMAC(rand16BytesKey, rand16BytesIv, inputInPlainText.getBytes()));
        requestParams.put("MPV", EncodingUtils.encodeAsBase64String("1"));
        requestParams.put("PKH", EncapCrypto.computePublicKeyHash(serverPubKeyBytes));

        return requestParams;
    }

    static String encryptAndSendMessage(TinkHttpClient client, String plainTextMessage) {
        byte[] key = RandomUtils.secureRandom(16);
        byte[] iv = RandomUtils.secureRandom(16);
        byte[] pubKeyBytes = EncodingUtils.decodeBase64String(EncapConstants.B64_ELLIPTIC_CURVE_PUBLIC_KEY);

        HashMap<String, String> cryptoRequestParams = getCryptoRequestParams(
                key, iv, pubKeyBytes, plainTextMessage);

        RequestBody encryptionRequestBody = new RequestBody(cryptoRequestParams);

        String response = client.request(EncapConstants.Urls.CRYPTO_EXCHANGE)
                .accept(MediaType.WILDCARD)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(String.class, encryptionRequestBody);

        Map<String, String> responseQueryPairs = EncapUtils.parseResponseQuery(response);

        String decryptedEMD = EncapCrypto.decryptEMDResponse(key, iv, responseQueryPairs.get("EMD"));
        boolean isVerified = EncapCrypto.verifyMACValue(key, iv, decryptedEMD, responseQueryPairs.get("MAC"));

        if (!isVerified) {
            throw new IllegalStateException("MAC authentication failed");
        }

        try {
            return new String(EncodingUtils.decodeHexString(decryptedEMD), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
