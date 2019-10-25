package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.encap;

import static se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.DEVICE_PROFILE;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Authentication;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.EncapMessage;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConfiguration;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConstants.DeviceInformation;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConstants.Message;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapStorage;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.entities.ActivatedMethodEntity;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.entities.IdentificationEntity;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.entities.RegistrationResultEntity;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc.ActivatedMethodsRequest;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc.IdentificationResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc.RegistrationResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.utils.EncapCryptoUtils;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.utils.EncapMessageUtils;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

/**
 * * Differences from the encap3 client:
 *
 * <p>1. No SOAP messages are being sent
 *
 * <p>2. Different encap host: "tunnistus.s-pankki.fi"
 *
 * <p>3. The encap messages differs
 */
public class SpankkiEncapClient {
    private final EncapConfiguration configuration;
    private final EncapStorage storage;
    private final SpankkiApiClient apiClient;
    private final String username;

    public SpankkiEncapClient(
            EncapConfiguration configuration,
            PersistentStorage persistentStorage,
            SpankkiApiClient apiClient,
            String username) {
        this.configuration = configuration;
        this.apiClient = apiClient;
        this.username = username;
        this.storage = new EncapStorage(persistentStorage);
    }

    public void registerDevice(String activationCode, String hardwareId) {
        storage.seedStorage(username);
        storage.setHardwareId(hardwareId);
        String registrationMessage = buildRegistrationMessage(activationCode);
        RegistrationResponse registrationResponse =
                encryptSendAndDecrypt(registrationMessage, RegistrationResponse.class);
        if (!registrationResponse.isValid()) {
            throw new IllegalStateException("ActivationResponse response is not valid.");
        }
        RegistrationResultEntity registrationResultEntity = registrationResponse.getResult();
        storeRegistrationResult(registrationResultEntity);

        String activationMessage = buildActivationMessage(registrationResultEntity);
        encryptSendAndDecrypt(activationMessage, RegistrationResponse.class);
    }

    public void authenticateDevice() {
        if (!storage.load()) {
            throw new IllegalStateException("Storage is not valid.");
        }
        String identificationMessage = buildIdentificationMessage();
        apiClient.startEncap();
        IdentificationResponse identificationResponse =
                encryptSendAndDecrypt(identificationMessage, IdentificationResponse.class);
        if (!identificationResponse.isValid()) {
            throw new IllegalStateException("IdentificationResponse is not valid.");
        }
        apiClient.pollEncap(); // implemented to imitiate app, might be unnecessary

        IdentificationEntity identificationEntity = identificationResponse.getResult();
        storeIdentificationResult(identificationEntity);

        String authenticationMessage = buildAuthenticationMessage(identificationEntity);
        encryptSendAndDecrypt(authenticationMessage, IdentificationResponse.class);

        pollEncap(); // implemented to imitiate app, might be unnecessary
    }

    public void saveDevice() {
        storage.save();
    }

    private <T> T encryptSendAndDecrypt(String plainTextMessage, Class<T> responseType) {
        byte[] key = RandomUtils.secureRandom(16);
        byte[] iv = RandomUtils.secureRandom(16);
        byte[] pubKeyBytes =
                EncodingUtils.decodeBase64String(Authentication.B64_ELLIPTIC_CURVE_PUBLIC_KEY);

        Map<String, String> cryptoRequestParams =
                EncapMessageUtils.getCryptoRequestParams(key, iv, pubKeyBytes, plainTextMessage);

        String response = apiClient.encap(cryptoRequestParams);

        Map<String, String> responseQueryPairs = EncapMessageUtils.parseResponseQuery(response);

        String decryptedEMD =
                EncapCryptoUtils.decryptEMDResponse(key, iv, responseQueryPairs.get("EMD"));

        boolean isVerified =
                EncapCryptoUtils.verifyMACValue(
                        key, iv, decryptedEMD, responseQueryPairs.get("MAC"));
        if (!isVerified) {
            throw new IllegalStateException("MAC authentication failed");
        }

        String decodedResponse = EncapMessageUtils.hexDecodeEmd(decryptedEMD);
        String jsonString = decodedResponse.replaceFirst("^\\)]}'", "");

        return SerializationUtils.deserializeFromString(jsonString, responseType);
    }

    private String buildRegistrationMessage(String activationCode) {
        Map<String, String> queryPairs = new HashMap<>();

        queryPairs.put("activationCode", activationCode);
        queryPairs.put("applicationId", EncapMessage.APPLICATION_ID);
        populateDeviceInformation(queryPairs);
        queryPairs.put("hexAPNToken", Message.HEX_APN_TOKEN);
        populateMetaInformation(queryPairs);
        queryPairs.put("operation", Message.OPERATION_REGISTER);

        return EncapMessageUtils.getUrlEncodedQueryParams(queryPairs);
    }

    private String buildActivationMessage(RegistrationResultEntity registrationResultEntity) {
        String otpChallenge = registrationResultEntity.getB64OtpChallenge();

        String b64challengeResponse =
                EncapCryptoUtils.computeB64ChallengeResponse(
                        storage.getAuthenticationKey(), otpChallenge);

        String b64challengeResponseWithoutPin =
                EncapCryptoUtils.computeB64ChallengeResponse(
                        storage.getAuthenticationKeyWithoutPin(), otpChallenge);

        ActivatedMethodEntity methodWithoutPin =
                new ActivatedMethodEntity(
                        Message.DEVICE,
                        storage.getAuthenticationKeyWithoutPin(),
                        b64challengeResponseWithoutPin);

        ActivatedMethodEntity methodWithPin =
                new ActivatedMethodEntity(
                        Message.DEVICE_PIN, storage.getAuthenticationKey(), b64challengeResponse);

        ActivatedMethodsRequest activatedMethods = new ActivatedMethodsRequest();
        activatedMethods.add(methodWithoutPin);
        activatedMethods.add(methodWithPin);

        String activatedMethodsRequest = SerializationUtils.serializeToString(activatedMethods);

        Map<String, String> queryPairs = new HashMap<>();

        queryPairs.put("activatedAuthMethods", activatedMethodsRequest);
        populateDeviceInformation(queryPairs);
        populateMetaInformation(queryPairs);
        queryPairs.put("operation", Message.OPERATION_ACTIVATE);
        queryPairs.put("registrationId", storage.getRegistrationId());
        queryPairs.put("saltHash1", storage.getSaltHash());
        queryPairs.put("signing.b64SigningCSR", configuration.getRsaPubKeyString());
        queryPairs.put("signing.b64SigningPubKey", configuration.getClientPrivateKeyString());

        return EncapMessageUtils.getUrlEncodedQueryParams(queryPairs);
    }

    private String buildIdentificationMessage() {
        Map<String, String> queryPairs = new HashMap<>();

        queryPairs.put("clientOnly", EncapMessage.CLIENT_ONLY);
        queryPairs.put("clientSaltCurrentKeyId", storage.getClientSaltKeyId());
        populateDeviceInformation(queryPairs);
        populateMetaInformation(queryPairs);
        queryPairs.put("operation", Message.OPERATION_IDENTIFY);
        queryPairs.put("purpose", Message.PURPOSE);
        queryPairs.put("registrationId", storage.getRegistrationId());

        return EncapMessageUtils.getUrlEncodedQueryParams(queryPairs);
    }

    private String buildAuthenticationMessage(IdentificationEntity identificationEntity) {
        String otpChallenge = identificationEntity.getB64OtpChallenge();
        String challengeResponse =
                EncapCryptoUtils.computeB64ChallengeResponse(
                        storage.getAuthenticationKey(), otpChallenge);

        Map<String, String> queryPairs = new HashMap<>();

        queryPairs.put("b64ResponseCurrent", challengeResponse);
        populateDeviceInformation(queryPairs);
        queryPairs.put("hexAPNToken", Message.HEX_APN_TOKEN);
        populateMetaInformation(queryPairs);
        queryPairs.put("operation", Message.OPERATION_AUTHENTICATE);
        queryPairs.put("registrationId", storage.getRegistrationId());
        queryPairs.put("saltHash1", storage.getSaltHash());
        queryPairs.put("usedAuthMethod", Message.DEVICE_PIN);

        return EncapMessageUtils.getUrlEncodedQueryParams(queryPairs);
    }

    private void populateDeviceInformation(Map<String, String> queryPairs) {
        queryPairs.put("device.ApplicationHash", buildApplicationHashAsB64String());
        queryPairs.put("device.DeviceHash", storage.getDeviceHash());
        queryPairs.put("device.DeviceManufacturer", DEVICE_PROFILE.getMake());
        queryPairs.put("device.DeviceModel", DEVICE_PROFILE.getModelNumber());
        queryPairs.put("device.DeviceName", DeviceInformation.NAME);
        queryPairs.put("device.DeviceUUID", storage.getDeviceUuid().toUpperCase());
        queryPairs.put("device.OperatingSystemName", DEVICE_PROFILE.getOs());
        queryPairs.put("device.OperatingSystemType", DEVICE_PROFILE.getOs());
        queryPairs.put("device.SignerHashes", DeviceInformation.SIGNER_HASHES);
        queryPairs.put("device.SystemVersion", DEVICE_PROFILE.getOsVersion());
        queryPairs.put("device.UserInterfaceIdiom", DeviceInformation.USER_INTERFACE_IDIOM);
    }

    private void populateMetaInformation(Map<String, String> queryPairs) {
        queryPairs.put("meta.encapAPIVersion", configuration.getEncapApiVersion());
    }

    // Implemented to imitate the app as much as possible, but it might be unnecessary
    private void pollEncap() {
        int MAX_ATTEMPTS = 10;
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            if (!apiClient.pollEncap().isPendingPerform()) {
                return;
            }
            Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
        }
    }

    private String buildApplicationHashAsB64String() {
        byte[] hash = Hash.sha256(configuration.getAppId());
        return EncodingUtils.encodeAsBase64String(hash);
    }

    private void storeRegistrationResult(RegistrationResultEntity registrationResultEntity) {
        int saltNextKeyId = registrationResultEntity.getClientSaltNextKeyId();
        storage.setClientSaltKeyId(saltNextKeyId);

        String saltNextKey = registrationResultEntity.getB64ClientSaltNextKey();
        storage.setClientSaltKey(saltNextKey);

        String registrationId = registrationResultEntity.getRegistration().getRegistrationId();
        storage.setRegistrationId(registrationId);

        String signingKeyPhrase = registrationResultEntity.getSigningKeyPhrase();
        storage.setSigningKeyPhrase(signingKeyPhrase);

        storage.setSamUserId("spankki"); // samUserId is not used anywhere by spankki but has to be
        // populated when loading storage in the EncapStorage class.
    }

    private void storeIdentificationResult(IdentificationEntity identificationEntity) {
        // Update storage with next keyId and key
        storage.setClientSaltKey(identificationEntity.getB64ClientSaltNextKey());
        storage.setClientSaltKeyId(identificationEntity.getClientSaltNextKeyId());
    }
}
