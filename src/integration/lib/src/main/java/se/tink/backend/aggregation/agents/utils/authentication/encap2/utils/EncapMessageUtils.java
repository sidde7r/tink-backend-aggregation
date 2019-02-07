package se.tink.backend.aggregation.agents.utils.authentication.encap2.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.EncapConfiguration;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.EncapConstants;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.EncapStorage;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.entities.IdentificationEntity;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.entities.RegistrationResultEntity;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.enums.AuthenticationMethod;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.rpc.RequestBody;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfile;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EncapMessageUtils {
    private final EncapConfiguration configuration;
    private final EncapStorage storage;
    private final TinkHttpClient httpClient;
    private final DeviceProfile deviceProfile;
    private final String applicationHash;

    public EncapMessageUtils(EncapConfiguration configuration, EncapStorage storage, TinkHttpClient httpClient,
            DeviceProfile deviceProfile) {
        this.configuration = configuration;
        this.storage = storage;
        this.httpClient = httpClient;
        this.deviceProfile = deviceProfile;
        this.applicationHash = buildApplicationHashAsB64String();
    }

    private String buildApplicationHashAsB64String() {
        byte[] hash = Hash.sha256(configuration.getAppId());
        return EncodingUtils.encodeAsBase64String(hash);
    }

    public String buildRegistrationMessage() {
        Map<String, String> queryPairs = new HashMap<>();

        queryPairs.put("applicationId", EncapConstants.Message.APPLICATION_ID);
        populateDeviceInformation(queryPairs);
        queryPairs.put("hexAPNToken", EncapConstants.Message.HEX_APN_TOKEN);
        populateMetaInformation(queryPairs);
        queryPairs.put("operation", EncapConstants.Message.OPERATION_REGISTER);
        queryPairs.put("response.requireToken", EncapConstants.Message.REQUIRE_TOKEN);

        return getUrlEncodedQueryParams(queryPairs);
    }

    public String buildActivationMessage(RegistrationResultEntity registrationResultEntity) {

        String otpChallenge = registrationResultEntity.getB64OtpChallenge();

        String b64ChallengeResponse = EncapCryptoUtils.computeB64ChallengeResponse(
                storage.getAuthenticationKey(),
                otpChallenge);

        String b64ChallengeResponseWithoutPin = EncapCryptoUtils.computeB64ChallengeResponse(
                storage.getAuthenticationKeyWithoutPin(),
                otpChallenge);

        Map<String, String> queryPairs = new HashMap<>();

        queryPairs.put("activatedAuthMethods", EncapConstants.Message.DEVICE_PIN);
        queryPairs.put("applicationId", EncapConstants.Message.APPLICATION_ID);
        queryPairs.put("b64AuthenticationKey", storage.getAuthenticationKey());
        queryPairs.put("b64AuthenticationKeyWithoutPin", storage.getAuthenticationKeyWithoutPin());
        queryPairs.put("b64ChallengeResponse", b64ChallengeResponse);
        queryPairs.put("b64ChallengeResponseWithoutPin", b64ChallengeResponseWithoutPin);
        queryPairs.put("b64TotpKey", storage.getTotpKey());
        populateDeviceInformation(queryPairs);
        populateMetaInformation(queryPairs);
        queryPairs.put("operation", EncapConstants.Message.OPERATION_ACTIVATE);
        queryPairs.put("registrationId", storage.getRegistrationId());
        queryPairs.put("saltHash", storage.getSaltHash());

        // Prepending string because the queryPairs map only handles unique keys
        return "activatedAuthMethods=DEVICE&" + getUrlEncodedQueryParams(queryPairs);
    }

    public String buildIdentificationMessage(@Nullable String authenticationId) {
        Map<String, String> queryPairs = new HashMap<>();

        queryPairs.put("applicationId", EncapConstants.Message.APPLICATION_ID);
        if (!Strings.isNullOrEmpty(authenticationId)) {
            queryPairs.put("clientData", authenticationId);
        }
        queryPairs.put("clientOnly", EncapConstants.Message.CLIENT_ONLY);
        queryPairs.put("clientSaltCurrentKeyId", storage.getClientSaltKeyId());
        populateDeviceInformation(queryPairs);
        populateMetaInformation(queryPairs);
        queryPairs.put("operation", EncapConstants.Message.OPERATION_IDENTIFY);
        queryPairs.put("purpose", EncapConstants.Message.PURPOSE);
        queryPairs.put("registrationId", storage.getRegistrationId());
        queryPairs.put("response.requireToken", EncapConstants.Message.REQUIRE_TOKEN);

        return getUrlEncodedQueryParams(queryPairs);
    }

    public String buildAuthenticationMessage(IdentificationEntity identificationEntity,
            AuthenticationMethod authenticationMethod) {
        String otpChallenge = identificationEntity.getB64OtpChallenge();

        String usedAuthMethod;
        String challengeResponse;
        if (authenticationMethod == AuthenticationMethod.DEVICE_AND_PIN) {
            challengeResponse = EncapCryptoUtils.computeB64ChallengeResponse(
                    storage.getAuthenticationKey(),
                    otpChallenge);
            usedAuthMethod = EncapConstants.Message.DEVICE_PIN;
        } else if (authenticationMethod == AuthenticationMethod.DEVICE) {
            challengeResponse = EncapCryptoUtils.computeB64ChallengeResponse(
                    storage.getAuthenticationKeyWithoutPin(),
                    otpChallenge);
            usedAuthMethod = EncapConstants.Message.DEVICE;
        } else {
            throw new IllegalStateException("Unknown encap authentication method.");
        }

        Map<String, String> queryPairs = new HashMap<>();

        queryPairs.put("applicationId", EncapConstants.Message.APPLICATION_ID);
        queryPairs.put("b64ResponseCurrent", challengeResponse);
        populateDeviceInformation(queryPairs);
        queryPairs.put("hexAPNToken", EncapConstants.Message.HEX_APN_TOKEN);
        populateMetaInformation(queryPairs);
        queryPairs.put("operation", EncapConstants.Message.OPERATION_AUTHENTICATE);
        queryPairs.put("registrationId", storage.getRegistrationId());
        queryPairs.put("saltHash1", storage.getSaltHash());
        queryPairs.put("usedAuthMethod", usedAuthMethod);

        return getUrlEncodedQueryParams(queryPairs);
    }

    private void populateDeviceInformation(Map<String, String> queryPairs) {
        queryPairs.put("device.ApplicationHash", applicationHash);
        queryPairs.put("device.DeviceHash", storage.getDeviceHash());
        queryPairs.put("device.DeviceManufacturer", deviceProfile.getMake());
        queryPairs.put("device.DeviceModel", deviceProfile.getModelNumber());
        queryPairs.put("device.DeviceName", EncapConstants.DeviceInformation.NAME);
        queryPairs.put("device.DeviceUUID", storage.getDeviceUuid());
        queryPairs.put("device.IsRootAvailable", EncapConstants.DeviceInformation.IS_ROOT_AVAILABLE);
        queryPairs.put("device.OperatingSystemName", deviceProfile.getOs());
        queryPairs.put("device.OperatingSystemType", deviceProfile.getOs());
        queryPairs.put("device.SignerHashes", EncapConstants.DeviceInformation.SIGNER_HASHES);
        queryPairs.put("device.SystemVersion", deviceProfile.getOsVersion());
        queryPairs.put("device.UserInterfaceIdiom", EncapConstants.DeviceInformation.USER_INTERFACE_IDIOM);
    }

    private void populateMetaInformation(Map<String, String> queryPairs) {
        queryPairs.put("meta.applicationVersion", configuration.getApplicationVersion());
        queryPairs.put("meta.encapAPIVersion", configuration.getEncapApiVersion());
    }

    private String getUrlEncodedQueryParams(Map<String, String> queryPairs) {
        Map<String, String> queryPairsWithUrlEncodedValues = Maps.newLinkedHashMap();
        queryPairs.forEach((key,value) -> queryPairsWithUrlEncodedValues.put(key, EncodingUtils.encodeUrl(value)));

        Joiner.MapJoiner joiner = Joiner.on("&").withKeyValueSeparator("=");
        return joiner.join(queryPairsWithUrlEncodedValues);
    }

    private HashMap<String, String> getCryptoRequestParams(byte[] rand16BytesKey, byte[] rand16BytesIv,
            byte[] serverPubKeyBytes, String inputInPlainText) {
        HashMap<String, String> requestParams = Maps.newHashMap();
        requestParams.put("EMD", EncapCryptoUtils.computeEMD(rand16BytesKey, rand16BytesIv, inputInPlainText.getBytes()));
        requestParams.put("EMK", EncapCryptoUtils.computeEMK(rand16BytesKey, rand16BytesIv, serverPubKeyBytes));
        requestParams.put("MAC", EncapCryptoUtils.computeMAC(rand16BytesKey, rand16BytesIv, inputInPlainText.getBytes()));
        requestParams.put("MPV", EncodingUtils.encodeAsBase64String("1"));
        requestParams.put("PKH", EncapCryptoUtils.computePublicKeyHash(serverPubKeyBytes));

        return requestParams;
    }

    public <T> T encryptAndSend(String plainTextMessage, Class<T> responseType) {
        byte[] key = RandomUtils.secureRandom(16);
        byte[] iv = RandomUtils.secureRandom(16);
        byte[] pubKeyBytes = EncodingUtils.decodeBase64String(EncapConstants.B64_ELLIPTIC_CURVE_PUBLIC_KEY);

        HashMap<String, String> cryptoRequestParams = getCryptoRequestParams(
                key, iv, pubKeyBytes, plainTextMessage);

        RequestBody encryptionRequestBody = new RequestBody(cryptoRequestParams);

        String response = httpClient.request(EncapConstants.Urls.CRYPTO_EXCHANGE)
                .accept(MediaType.WILDCARD)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(String.class, encryptionRequestBody);

        Map<String, String> responseQueryPairs = parseResponseQuery(response);

        String decryptedEMD = EncapCryptoUtils.decryptEMDResponse(key, iv, responseQueryPairs.get("EMD"));
        boolean isVerified = EncapCryptoUtils.verifyMACValue(key, iv, decryptedEMD, responseQueryPairs.get("MAC"));

        if (!isVerified) {
            throw new IllegalStateException("MAC authentication failed");
        }

        String decodedResponse = hexDecodeEmd(decryptedEMD);
        String jsonString = decodedResponse.replaceFirst("^\\)]}'", "");

        return SerializationUtils.deserializeFromString(jsonString, responseType);
    }

    private String hexDecodeEmd(String decryptedEmd) {
        try {
            return new String(EncodingUtils.decodeHexString(decryptedEmd), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private Map<String, String> parseResponseQuery(String responseQuery) {
        Map<String, String> queryPairs = Maps.newHashMap();
        String[] pairs = responseQuery.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            queryPairs.put(EncodingUtils.decodeUrl(pair.substring(0, idx)).toUpperCase(),
                    EncodingUtils.decodeUrl(pair.substring(idx + 1)));
        }

        return queryPairs;
    }
}
