package se.tink.backend.aggregation.agents.utils.authentication.encap3.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.primitives.Bytes;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConfiguration;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConstants;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.entities.ActivatedMethodEntity;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.entities.IdentificationEntity;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.entities.RegistrationResultEntity;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.enums.AuthenticationMethod;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc.ActivatedMethodsRequest;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc.EncryptedSoapRequestBody;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc.EncryptedSoapResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc.RequestBody;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.storage.BaseEncapStorage;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfile;
import se.tink.libraries.serialization.utils.SerializationUtils;

public abstract class BaseEncapMessageUtils implements EncapMessageUtils {

    protected final TinkHttpClient httpClient;
    protected final EncapConfiguration configuration;

    private final BaseEncapStorage storage;
    private final DeviceProfile deviceProfile;
    private final String applicationHash;

    public BaseEncapMessageUtils(
            EncapConfiguration configuration,
            BaseEncapStorage storage,
            TinkHttpClient httpClient,
            DeviceProfile deviceProfile) {
        this.configuration = configuration;
        this.storage = storage;
        this.httpClient = httpClient;
        this.deviceProfile = deviceProfile;
        this.applicationHash = buildApplicationHashAsB64String();
    }

    protected abstract byte[] prepareRandomKey(int size);

    protected abstract String generateRandomHex();

    protected abstract KeyPair generateEcKeyPair();

    protected abstract String getEmk(byte[] randKey);

    private String buildApplicationHashAsB64String() {
        byte[] hash = Hash.sha256(configuration.getAppId());
        return EncodingUtils.encodeAsBase64String(hash);
    }

    public static String getUrlEncodedQueryParams(Map<String, String> queryPairs) {
        Map<String, String> queryPairsWithUrlEncodedValues = Maps.newLinkedHashMap();
        queryPairs.forEach(
                (key, value) ->
                        queryPairsWithUrlEncodedValues.put(key, EncodingUtils.encodeUrl(value)));

        Joiner.MapJoiner joiner = Joiner.on("&").withKeyValueSeparator("=");
        return joiner.join(queryPairsWithUrlEncodedValues);
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

    public static Map<String, String> getCryptoRequestParams(
            byte[] rand16BytesKey,
            byte[] rand16BytesIv,
            byte[] serverPubKeyBytes,
            String inputInPlainText,
            KeyPair ecKeyPair) {
        Map<String, String> requestParams = Maps.newHashMap();
        requestParams.put(
                "EMD",
                EncapCryptoUtils.encrypAesCbc(
                        rand16BytesKey, rand16BytesIv, inputInPlainText.getBytes()));
        requestParams.put(
                "EMK",
                EncapCryptoUtils.computeEMK(
                        ecKeyPair, rand16BytesKey, rand16BytesIv, serverPubKeyBytes));
        requestParams.put(
                "MAC",
                EncapCryptoUtils.computeMAC(
                        rand16BytesKey, rand16BytesIv, inputInPlainText.getBytes()));
        requestParams.put("MPV", EncodingUtils.encodeAsBase64String("1"));
        requestParams.put("PKH", EncapCryptoUtils.computePublicKeyHash(serverPubKeyBytes));

        return requestParams;
    }

    @Override
    public String buildRegistrationMessage() {
        Map<String, String> queryPairs = new HashMap<>();

        queryPairs.put(EncapConstants.MessageConstants.APPLICATION_ID, EncapConstants.Message.APPLICATION_ID);
        populateDeviceInformation(queryPairs);
        queryPairs.put(EncapConstants.MessageConstants.HEX_APN_TOKEN, EncapConstants.Message.HEX_APN_TOKEN);
        populateMetaInformation(queryPairs);
        queryPairs.put(EncapConstants.MessageConstants.OPERATION, EncapConstants.Message.OPERATION_REGISTER);

        return getUrlEncodedQueryParams(queryPairs);
    }

    protected void populateDeviceInformation(Map<String, String> queryPairs) {
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

    protected void populateMetaInformation(Map<String, String> queryPairs) {
        queryPairs.put("meta.encapAPIVersion", configuration.getEncapApiVersion());
    }

    @Override
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
                        EncapConstants.Message.DEVICE,
                        storage.getAuthenticationKeyWithoutPin(),
                        b64challengeResponseWithoutPin);

        ActivatedMethodEntity methodWithPin =
                new ActivatedMethodEntity(
                        EncapConstants.Message.DEVICE_PIN,
                        storage.getAuthenticationKey(),
                        b64challengeResponse);

        ActivatedMethodsRequest activatedMethods = new ActivatedMethodsRequest();
        activatedMethods.add(methodWithoutPin);
        activatedMethods.add(methodWithPin);

        String activatedMethodsRequest = SerializationUtils.serializeToString(activatedMethods);

        Map<String, String> queryPairs = new HashMap<>();

        queryPairs.put(EncapConstants.MessageConstants.ACTIVATED_AUTH_METHODS, activatedMethodsRequest);
        populateDeviceInformation(queryPairs);
        populateMetaInformation(queryPairs);
        queryPairs.put(EncapConstants.MessageConstants.OPERATION, EncapConstants.Message.OPERATION_ACTIVATE);
        queryPairs.put(EncapConstants.Storage.REGISTRATION_ID, storage.getRegistrationId());
        queryPairs.put(EncapConstants.Storage.B64_SALT_HASH, storage.getSaltHash());

        // Prepending string because the queryPairs map only handles unique keys
        return getUrlEncodedQueryParams(queryPairs);
    }

    @Override
    public String buildIdentificationMessage(@Nullable String authenticationId) {
        Map<String, String> queryPairs = new HashMap<>();

        if (!Strings.isNullOrEmpty(authenticationId)) {
            queryPairs.put(EncapConstants.MessageConstants.CLIENT_DATA, authenticationId);
        }
        queryPairs.put(EncapConstants.MessageConstants.CLIENT_ONLY, EncapConstants.Message.CLIENT_ONLY);
        queryPairs.put(EncapConstants.Storage.CLIENT_SALT_CURRENT_KEY_ID, storage.getClientSaltKeyId());
        populateDeviceInformation(queryPairs);
        populateMetaInformation(queryPairs);
        queryPairs.put(EncapConstants.MessageConstants.OPERATION, EncapConstants.Message.OPERATION_IDENTIFY);
        queryPairs.put(EncapConstants.MessageConstants.PURPOSE, EncapConstants.Message.PURPOSE);
        queryPairs.put(EncapConstants.Storage.REGISTRATION_ID, storage.getRegistrationId());

        return getUrlEncodedQueryParams(queryPairs);
    }

    @Override
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

        queryPairs.put(EncapConstants.MessageConstants.B64_RESPONSE_CURRENT, challengeResponse);
        populateDeviceInformation(queryPairs);
        queryPairs.put(EncapConstants.MessageConstants.HEX_APN_TOKEN, EncapConstants.Message.HEX_APN_TOKEN);
        populateMetaInformation(queryPairs);
        queryPairs.put(EncapConstants.MessageConstants.OPERATION, EncapConstants.Message.OPERATION_AUTHENTICATE);
        queryPairs.put(EncapConstants.Storage.REGISTRATION_ID, storage.getRegistrationId());
        queryPairs.put(EncapConstants.Storage.SALT_HASH_1, storage.getSaltHash());
        queryPairs.put(EncapConstants.MessageConstants.USED_AUTH_METHOD, usedAuthMethod);

        return getUrlEncodedQueryParams(queryPairs);
    }

    @Override
    public String encryptSoapAndSend(URL url, String soapMessage) {
        byte[] randKeyBytes = prepareRandomKey(32);
        byte[] hmacDataBytes = Base64.getDecoder().decode(EncapConstants.Soap.MAC_B64);
        byte[] aesKeyDataBytes = Base64.getDecoder().decode(EncapConstants.Soap.ENC_B64);
        byte[] hmacKeyBytes = Hash.hmacSha256(randKeyBytes, hmacDataBytes);
        byte[] aesKeyBytes = Hash.hmacSha256(randKeyBytes, aesKeyDataBytes);

        EncryptedSoapRequestBody body =
                getSoapCryptoRequestBody(randKeyBytes, aesKeyBytes, hmacKeyBytes, soapMessage);

        EncryptedSoapResponse response =
                httpClient
                        .request(url)
                        .header(
                                EncapConstants.HttpHeaders.EVRY_CLIENTNAME_KEY,
                                EncapConstants.HttpHeaders.EVRY_CLIENTNAME_VALUE)
                        .header(EncapConstants.HttpHeaders.EVRY_REQUESTID, generateRandomHex())
                        .header(
                                EncapConstants.HttpHeaders.USER_AGENT_KEY,
                                EncapConstants.HttpHeaders.USER_AGENT_VALUE)
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

    private EncryptedSoapRequestBody getSoapCryptoRequestBody(
            byte[] randKey, byte[] aesKey, byte[] hmacKey, String soapMessage) {
        byte[] randIv = prepareRandomKey(32);
        byte[] payload = soapMessage.getBytes();
        byte[] headers = Base64.getDecoder().decode(EncapConstants.Soap.HEADERS_B64);

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
        String emk = getEmk(randKey);

        EncryptedSoapRequestBody requestBody = new EncryptedSoapRequestBody();
        requestBody.setIv(randIvB64);
        requestBody.setPayload(ecryptedPayloadB64);
        requestBody.setHeaders(encryptedHeadersB64);
        requestBody.setMac(mac);
        requestBody.setEmk(emk);

        return requestBody;
    }

    @Override
    public <T> T encryptAndSend(String plainTextMessage, Class<T> responseType) {
        byte[] key = prepareRandomKey(16);
        byte[] iv = prepareRandomKey(16);
        byte[] pubKeyBytes =
                EncodingUtils.decodeBase64String(EncapConstants.B64_ELLIPTIC_CURVE_PUBLIC_KEY);

        Map<String, String> cryptoRequestParams =
                getCryptoRequestParams(key, iv, pubKeyBytes, plainTextMessage, generateEcKeyPair());

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
}
