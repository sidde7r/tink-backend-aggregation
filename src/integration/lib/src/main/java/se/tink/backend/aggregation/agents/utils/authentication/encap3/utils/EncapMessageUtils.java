package se.tink.backend.aggregation.agents.utils.authentication.encap3.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.primitives.Bytes;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConfiguration;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConstants;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConstants.HttpHeaders;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConstants.Soap;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapStorage;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.entities.ActivatedMethodEntity;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.entities.IdentificationEntity;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.entities.RegistrationResultEntity;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.enums.AuthenticationMethod;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc.ActivatedMethodsRequest;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc.EncryptedSoapRequestBody;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc.EncryptedSoapResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc.RequestBody;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfile;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EncapMessageUtils {
    private final EncapConfiguration configuration;
    private final EncapStorage storage;
    private final TinkHttpClient httpClient;
    private final DeviceProfile deviceProfile;
    private final String applicationHash;

    public EncapMessageUtils(
            EncapConfiguration configuration,
            EncapStorage storage,
            TinkHttpClient httpClient,
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

        return getUrlEncodedQueryParams(queryPairs);
    }

    public String buildActivationMessage(RegistrationResultEntity registrationResultEntity) {
        String otpChallenge = registrationResultEntity.getB64OtpChallenge();

        String b64challengeResponse =
                EncapCryptoUtils.computeB64ChallengeResponse(
                        storage.getAuthenticationKey(), otpChallenge);

        String b64challengeResponseWithoutPin =
                EncapCryptoUtils.computeB64ChallengeResponse(
                        storage.getAuthenticationKeyWithoutPin(), otpChallenge);

        ActivatedMethodEntity methodWithoutPin =
                new ActivatedMethodEntity(
                        "DEVICE",
                        storage.getAuthenticationKeyWithoutPin(),
                        b64challengeResponseWithoutPin);

        ActivatedMethodEntity methodWithPin =
                new ActivatedMethodEntity(
                        "DEVICE:PIN", storage.getAuthenticationKey(), b64challengeResponse);

        ActivatedMethodsRequest activatedMethods = new ActivatedMethodsRequest();
        activatedMethods.add(methodWithoutPin);
        activatedMethods.add(methodWithPin);

        String activatedMethodsRequest = SerializationUtils.serializeToString(activatedMethods);

        Map<String, String> queryPairs = new HashMap<>();

        queryPairs.put("activatedAuthMethods", activatedMethodsRequest);
        populateDeviceInformation(queryPairs);
        populateMetaInformation(queryPairs);
        queryPairs.put("operation", EncapConstants.Message.OPERATION_ACTIVATE);
        queryPairs.put("registrationId", storage.getRegistrationId());
        queryPairs.put("saltHash", storage.getSaltHash());

        // Prepending string because the queryPairs map only handles unique keys
        return getUrlEncodedQueryParams(queryPairs);
    }

    public String buildIdentificationMessage(@Nullable String authenticationId) {
        Map<String, String> queryPairs = new HashMap<>();

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

        return getUrlEncodedQueryParams(queryPairs);
    }

    public String buildAuthenticationMessage(
            IdentificationEntity identificationEntity, AuthenticationMethod authenticationMethod) {
        String otpChallenge = identificationEntity.getB64OtpChallenge();

        String usedAuthMethod;
        String challengeResponse;
        if (authenticationMethod == AuthenticationMethod.DEVICE_AND_PIN) {
            challengeResponse =
                    EncapCryptoUtils.computeB64ChallengeResponse(
                            storage.getAuthenticationKey(), otpChallenge);
            usedAuthMethod = EncapConstants.Message.DEVICE_PIN;
        } else if (authenticationMethod == AuthenticationMethod.DEVICE) {
            challengeResponse =
                    EncapCryptoUtils.computeB64ChallengeResponse(
                            storage.getAuthenticationKeyWithoutPin(), otpChallenge);
            usedAuthMethod = EncapConstants.Message.DEVICE;
        } else {
            throw new IllegalStateException("Unknown encap authentication method.");
        }

        Map<String, String> queryPairs = new HashMap<>();

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
        queryPairs.put("device.OperatingSystemName", deviceProfile.getOs());
        queryPairs.put("device.OperatingSystemType", deviceProfile.getOs());
        queryPairs.put("device.SignerHashes", EncapConstants.DeviceInformation.SIGNER_HASHES);
        queryPairs.put("device.SystemVersion", deviceProfile.getOsVersion());
        queryPairs.put(
                "device.UserInterfaceIdiom", EncapConstants.DeviceInformation.USER_INTERFACE_IDIOM);
    }

    private void populateMetaInformation(Map<String, String> queryPairs) {
        queryPairs.put("meta.encapAPIVersion", configuration.getEncapApiVersion());
    }

    public static String getUrlEncodedQueryParams(Map<String, String> queryPairs) {
        Map<String, String> queryPairsWithUrlEncodedValues = Maps.newLinkedHashMap();
        queryPairs.forEach(
                (key, value) ->
                        queryPairsWithUrlEncodedValues.put(key, EncodingUtils.encodeUrl(value)));

        Joiner.MapJoiner joiner = Joiner.on("&").withKeyValueSeparator("=");
        return joiner.join(queryPairsWithUrlEncodedValues);
    }

    public static Map<String, String> getCryptoRequestParams(
            byte[] rand16BytesKey,
            byte[] rand16BytesIv,
            byte[] serverPubKeyBytes,
            String inputInPlainText) {
        HashMap<String, String> requestParams = Maps.newHashMap();
        requestParams.put(
                "EMD",
                EncapCryptoUtils.encrypAesCbc(
                        rand16BytesKey, rand16BytesIv, inputInPlainText.getBytes()));
        requestParams.put(
                "EMK",
                EncapCryptoUtils.computeEMK(rand16BytesKey, rand16BytesIv, serverPubKeyBytes));
        requestParams.put(
                "MAC",
                EncapCryptoUtils.computeMAC(
                        rand16BytesKey, rand16BytesIv, inputInPlainText.getBytes()));
        requestParams.put("MPV", EncodingUtils.encodeAsBase64String("1"));
        requestParams.put("PKH", EncapCryptoUtils.computePublicKeyHash(serverPubKeyBytes));

        return requestParams;
    }

    private EncryptedSoapRequestBody getSoapCryptoRequestBody(
            byte[] randKey, byte[] aesKey, byte[] hmacKey, String soapMessage) {
        byte[] randIv = RandomUtils.secureRandom(32);
        byte[] payload = soapMessage.getBytes();
        byte[] headers = Base64.getDecoder().decode(Soap.HEADERS_B64);

        String randIvB64 = Base64.getEncoder().encodeToString(randIv);
        String ecryptedPayloadB64 =
                EncapCryptoUtils.encrypAesCbc(aesKey, Arrays.copyOfRange(randIv, 0, 16), payload);
        String encryptedHeadersB64 =
                EncapCryptoUtils.encrypAesCbc(aesKey, Arrays.copyOfRange(randIv, 16, 32), headers);
        String mac =
                EncapCryptoUtils.computeMAC(
                        Arrays.copyOfRange(hmacKey, 0, 16),
                        Arrays.copyOfRange(hmacKey, 16, 32),
                        Bytes.concat(
                                encryptedHeadersB64.getBytes(),
                                ecryptedPayloadB64.getBytes(),
                                randIvB64.getBytes()));
        String emk = EncapCryptoUtils.computeRsaEMK(configuration.getRsaPubKeyString(), randKey);

        EncryptedSoapRequestBody requestBody = new EncryptedSoapRequestBody();
        requestBody.setIv(randIvB64);
        requestBody.setPayload(ecryptedPayloadB64);
        requestBody.setHeaders(encryptedHeadersB64);
        requestBody.setMac(mac);
        requestBody.setEmk(emk);

        return requestBody;
    }

    public String encryptSoapAndSend(URL url, String soapMessage) {
        byte[] randKeyBytes = RandomUtils.secureRandom(32);
        byte[] hmacDataBytes = Base64.getDecoder().decode(Soap.MAC_B64);
        byte[] aesKeyDataBytes = Base64.getDecoder().decode(Soap.ENC_B64);
        byte[] hmacKeyBytes = Hash.hmacSha256(randKeyBytes, hmacDataBytes);
        byte[] aesKeyBytes = Hash.hmacSha256(randKeyBytes, aesKeyDataBytes);

        EncryptedSoapRequestBody body =
                getSoapCryptoRequestBody(randKeyBytes, aesKeyBytes, hmacKeyBytes, soapMessage);

        EncryptedSoapResponse response =
                httpClient
                        .request(url)
                        .header(HttpHeaders.EVRY_CLIENTNAME_KEY, HttpHeaders.EVRY_CLIENTNAME_VALUE)
                        .header(
                                HttpHeaders.EVRY_REQUESTID,
                                RandomUtils.generateRandomHexEncoded(4).toUpperCase())
                        .header(HttpHeaders.USER_AGENT_KEY, HttpHeaders.USER_AGENT_VALUE)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.WILDCARD)
                        .acceptLanguage(Locale.US)
                        .post(EncryptedSoapResponse.class, body);

        String hmacData = response.getHeaders() + response.getPayload() + response.getIv();
        String calculatedMac =
                EncapCryptoUtils.computeMAC(
                        Arrays.copyOfRange(hmacKeyBytes, 0, 16),
                        Arrays.copyOfRange(hmacKeyBytes, 16, 32),
                        hmacData.getBytes());

        if (!calculatedMac.equals(response.getMac())) {
            throw new IllegalStateException("MAC authentication failed");
        }

        byte[] aesIvBytes = Base64.getDecoder().decode(response.getIv());

        return EncapCryptoUtils.decryptPayloadResponse(
                aesKeyBytes, Arrays.copyOfRange(aesIvBytes, 0, 16), response.getPayload());
    }

    public <T> T encryptAndSend(String plainTextMessage, Class<T> responseType) {
        byte[] key = RandomUtils.secureRandom(16);
        byte[] iv = RandomUtils.secureRandom(16);
        byte[] pubKeyBytes =
                EncodingUtils.decodeBase64String(EncapConstants.B64_ELLIPTIC_CURVE_PUBLIC_KEY);

        Map<String, String> cryptoRequestParams =
                getCryptoRequestParams(key, iv, pubKeyBytes, plainTextMessage);

        String response =
                httpClient
                        .request(EncapConstants.Urls.CRYPTO_EXCHANGE)
                        .accept(MediaType.WILDCARD)
                        .type(MediaType.APPLICATION_FORM_URLENCODED)
                        .post(String.class, new RequestBody(cryptoRequestParams));

        Map<String, String> responseQueryPairs = parseResponseQuery(response);

        String decryptedEMD =
                EncapCryptoUtils.decryptEMDResponse(key, iv, responseQueryPairs.get("EMD"));
        boolean isVerified =
                EncapCryptoUtils.verifyMACValue(
                        key, iv, decryptedEMD, responseQueryPairs.get("MAC"));

        if (!isVerified) {
            throw new IllegalStateException("MAC authentication failed");
        }

        String decodedResponse = hexDecodeEmd(decryptedEMD);
        String jsonString = decodedResponse.replaceFirst("^\\)]}'", "");

        return SerializationUtils.deserializeFromString(jsonString, responseType);
    }

    public static String hexDecodeEmd(String decryptedEmd) {
        return new String(EncodingUtils.decodeHexString(decryptedEmd), StandardCharsets.UTF_8);
    }

    public static Map<String, String> parseResponseQuery(String responseQuery) {
        Map<String, String> queryPairs = Maps.newHashMap();
        String[] pairs = responseQuery.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            queryPairs.put(
                    EncodingUtils.decodeUrl(pair.substring(0, idx)).toUpperCase(),
                    EncodingUtils.decodeUrl(pair.substring(idx + 1)));
        }

        return queryPairs;
    }
}
