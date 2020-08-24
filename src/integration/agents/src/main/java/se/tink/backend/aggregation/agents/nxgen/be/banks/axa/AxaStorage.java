package se.tink.backend.aggregation.agents.nxgen.be.banks.axa;

import java.security.KeyPair;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.assertj.core.util.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.MethodEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.OtpFormatEntity.Challenge;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.AnonymousInvokeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.AssertFormResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.BindResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.LogonResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.utils.AxaCryptoUtil;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class AxaStorage {

    private static final String LANGUAGE = "language";
    private static final String CARD_NUMBER = "cardNumber";
    private static final String DEVICE_ID = "deviceId";
    private static final String DEVICE_NAME = "deviceName";
    private static final String PARAMS_SESSION_ID = "paramsSessionId";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String TOKEN = "token";
    private static final String ASSERTION_ID = "assertionId";
    private static final String RESPONSE_CHALLENGE = "responseChallenge";
    private static final String IS_DEVICE_REGISTERED = "isDeviceRegistered";
    private static final String HEADER_DEVICE_ID = "headerDeviceId";
    private static final String HEADER_SESSION_ID = "headerSessionId";
    private static final String UID = "uid";
    private static final String OTP_CHALLENGE = "otpChallenge";
    private static final String PARTIAL_CARD_NUMBER_CHALLENGE = "partialCardNumberChallenge";
    private static final String RSA_PUBLIC_KEY = "rsaPublicKey";
    private static final String RSA_PRIVATE_KEY = "rsaPrivateKey";
    private static final String EC_REQUEST_SIGN_PUBLIC_KEY = "ecRequestSignPublicKey";
    private static final String EC_REQUEST_SIGN_PRIVATE_KEY = "ecRequestSignPrivateKey";
    private static final String EC_CHALLENGE_SIGN_PUBLIC_KEY = "ecChallengeSignPublicKey";
    private static final String EC_CHALLENGE_SIGN_PRIVATE_KEY = "ecChallengeSignPrivateKey";
    private static final String CARD_READER_RESPONSE = "cardReaderResponse";
    private static final String PAN_SEQUENCE_NUMBER = "panSequenceNumber";
    private static final String TRANSMIT_TICKET_ID = "transmitTicketId";
    private static final String FIRST_NAME = "firstName";
    private static final String CUSTOMER_ID = "customerId";
    private static final String BATCH_INSTALLATION_ID = "batchInstallationId";

    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public AxaStorage(
            final SessionStorage sessionStorage, final PersistentStorage persistentStorage) {
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    public void persistLanguage(@Nonnull final String language) {
        Preconditions.checkNotNull(language);
        persistentStorage.put(LANGUAGE, language);
    }

    public void persistCardNumber(@Nonnull final String cardNumber) {
        persistentStorage.put(CARD_NUMBER, cardNumber);
    }

    public String getDeviceId() {
        return persistentStorage.get(DEVICE_ID);
    }

    public String getDeviceName() {
        return persistentStorage.get(DEVICE_NAME);
    }

    public void storeParamsSessionId(String paramsSessionId) {
        persistentStorage.put(PARAMS_SESSION_ID, paramsSessionId);
    }

    public String getParamsSessionId() {
        return persistentStorage.get(PARAMS_SESSION_ID);
    }

    public Optional<Integer> getCustomerId() {
        return persistentStorage.get(CUSTOMER_ID, Integer.class);
    }

    public Optional<String> getAccessToken() {
        return Optional.ofNullable(sessionStorage.get(ACCESS_TOKEN));
    }

    public Optional<String> getLanguage() {
        return Optional.ofNullable(persistentStorage.get(LANGUAGE));
    }

    public String getCardNumber() {
        return persistentStorage.get(CARD_NUMBER);
    }

    public String getCardNumberChallenge() {
        return sessionStorage
                .get(PARTIAL_CARD_NUMBER_CHALLENGE, String.class)
                .orElseThrow(IllegalStateException::new);
    }

    public void registerDevice() {
        persistentStorage.put(IS_DEVICE_REGISTERED, true);
    }

    public boolean isDeviceRegistered() {
        return persistentStorage.get(IS_DEVICE_REGISTERED, Boolean.class).orElse(Boolean.FALSE);
    }

    public void storeValues(AnonymousInvokeResponse response) {
        sessionStorage.put(RESPONSE_CHALLENGE, response.getData().getChallenge());
        sessionStorage.put(
                ASSERTION_ID, response.getData().getControlFlow().get(0).getAssertionId());
        persistentStorage.put(UID, response.getUid());
        persistentStorage.put(HEADER_DEVICE_ID, response.getDeviceId());
        persistentStorage.put(HEADER_SESSION_ID, response.getSessionId());
    }

    public String getAssertionId() {
        return sessionStorage.get(ASSERTION_ID);
    }

    public String getFch() {
        return sessionStorage.get(RESPONSE_CHALLENGE);
    }

    public String getUid() {
        return persistentStorage.get(UID);
    }

    public String getDeviceIdFromHeader() {
        return persistentStorage.get(HEADER_DEVICE_ID);
    }

    public String getSessionIdFromHeader() {
        return persistentStorage.get(HEADER_SESSION_ID);
    }

    public void storeValuesFromCardNumberAssertFormResponse(AssertFormResponse response) {
        persistentStorage.put(UID, response.getData().getData().getJsonData().getGuid());
        sessionStorage.put(
                TRANSMIT_TICKET_ID,
                response.getData().getData().getJsonData().getTransmitTicketId());
        sessionStorage.put(
                PAN_SEQUENCE_NUMBER,
                response.getData().getData().getJsonData().getPanSequenceNumber());
        sessionStorage.put(FIRST_NAME, response.getData().getData().getJsonData().getFirstName());
    }

    public String getOtpChallenge() {
        return sessionStorage.get(OTP_CHALLENGE);
    }

    public String getCardReaderResponse() {
        return sessionStorage.get(CARD_READER_RESPONSE);
    }

    public void storeCardReaderResponse(String cardReaderResponse) {
        sessionStorage.put(CARD_READER_RESPONSE, cardReaderResponse);
    }

    public void storeValuesFromFirstOtpResponse(AssertFormResponse response) {
        sessionStorage.put(
                ASSERTION_ID, response.getData().getControlFlow().get(0).getAssertionId());
    }

    public String getPanSequenceNumber() {
        return sessionStorage.get(PAN_SEQUENCE_NUMBER);
    }

    public String getTransmitTicketId() {
        return sessionStorage.get(TRANSMIT_TICKET_ID);
    }

    public String getFirstName() {
        return sessionStorage.get(FIRST_NAME);
    }

    public void storeValuesFromSecondOtpResponse(AssertFormResponse response) {
        sessionStorage.put(
                ASSERTION_ID,
                response.getData().getControlFlow().get(0).getAssertions().get(0).getAssertionId());
    }

    public void storeValuesFromBindResponse(BindResponse response) {
        sessionStorage.put(
                ASSERTION_ID,
                response.getData()
                        .getControlFlow()
                        .get(0)
                        .getMethods()
                        .get(0)
                        .getChannels()
                        .get(0)
                        .getAssertionId());
        sessionStorage.put(
                OTP_CHALLENGE,
                response.getData()
                        .getControlFlow()
                        .get(0)
                        .getMethods()
                        .get(0)
                        .getOtpFormat()
                        .getData()
                        .getChallenge());
        sessionStorage.put(RESPONSE_CHALLENGE, response.getData().getChallenge());
        persistentStorage.put(HEADER_DEVICE_ID, response.getDeviceId());
        persistentStorage.put(HEADER_SESSION_ID, response.getSessionId());
    }

    public void storeValuesFromProfileNameAssertFormResponse(AssertFormResponse response) {
        List<Challenge> challengeList =
                response.getData().getData().getOtpFormat().getData().getChallengeList();
        String otpChallenge =
                challengeList.stream()
                        .filter(challenge -> challenge.getChallenge() != null)
                        .findFirst()
                        .map(Challenge::getChallenge)
                        .orElseThrow(NoSuchElementException::new);
        String partialCardNumber =
                challengeList.stream()
                        .filter(challenge -> challenge.getCard() != null)
                        .findFirst()
                        .map(Challenge::getCard)
                        .orElseThrow(NoSuchElementException::new);

        sessionStorage.put(OTP_CHALLENGE, otpChallenge);
        sessionStorage.put(PARTIAL_CARD_NUMBER_CHALLENGE, partialCardNumber);
        sessionStorage.put(
                ASSERTION_ID,
                response.getData()
                        .getControlFlow()
                        .get(0)
                        .getMethods()
                        .get(0)
                        .getChannels()
                        .get(0)
                        .getAssertionId());
    }

    public void storeValuesFromAssertRegistrationResponse(AssertFormResponse response) {
        sessionStorage.put(TOKEN, response.getData().getToken());
    }

    public void storeValuesFromAssertConfirmationResponse(AssertFormResponse response) {
        sessionStorage.put(TOKEN, response.getData().getToken());
    }

    public void storeValuesFromLoginResponse(LoginResponse response) {
        String assertionId =
                response.getData().getControlFlow().get(0).getMethods().stream()
                        .filter(method -> "pin".equals(method.getType()))
                        .findFirst()
                        .map(MethodEntity::getAssertionId)
                        .orElseThrow(NoSuchElementException::new);
        sessionStorage.put(ASSERTION_ID, assertionId);
        sessionStorage.put(RESPONSE_CHALLENGE, response.getData().getChallenge());
        persistentStorage.put(HEADER_SESSION_ID, response.getSessionId());
    }

    public void storeValuesFromAssertPinAuthenticationResponse(AssertFormResponse response) {
        sessionStorage.put(TOKEN, response.getData().getToken());
        persistentStorage.put(HEADER_SESSION_ID, response.getSessionId());
    }

    public void storeValuesFromLogonResponse(LogonResponse response) {
        persistentStorage.put(
                CUSTOMER_ID, response.getCustomerId().orElseThrow(NoSuchElementException::new));
        sessionStorage.put(
                ACCESS_TOKEN, response.getAccessToken().orElseThrow(NoSuchElementException::new));
    }

    public void storeRSAKeyPair(KeyPair rsaKeyPair) {
        storeKeyPair(rsaKeyPair, RSA_PUBLIC_KEY, RSA_PRIVATE_KEY);
    }

    public void storeChallengeSignECKeyPair(KeyPair challengeSignECKeyPair) {
        storeKeyPair(
                challengeSignECKeyPair,
                EC_CHALLENGE_SIGN_PUBLIC_KEY,
                EC_CHALLENGE_SIGN_PRIVATE_KEY);
    }

    public void storeRequestSignatureECKeyPair(KeyPair requestSignatureECKeyPair) {
        storeKeyPair(
                requestSignatureECKeyPair, EC_REQUEST_SIGN_PUBLIC_KEY, EC_REQUEST_SIGN_PRIVATE_KEY);
    }

    public KeyPair getChallengeSignECKeyPair() {
        return getECKeyPair(EC_CHALLENGE_SIGN_PUBLIC_KEY, EC_CHALLENGE_SIGN_PRIVATE_KEY);
    }

    public KeyPair getRequestSignatureECKeyPair() {
        return getECKeyPair(EC_REQUEST_SIGN_PUBLIC_KEY, EC_REQUEST_SIGN_PRIVATE_KEY);
    }

    public String getRequestSignatureECPublicKey() {
        return persistentStorage.get(EC_REQUEST_SIGN_PUBLIC_KEY);
    }

    public String getEncodedRSAPublicKey() {
        return persistentStorage.get(RSA_PUBLIC_KEY);
    }

    public String getEncodedRSAPrivateKey() {
        return persistentStorage.get(RSA_PRIVATE_KEY);
    }

    public void storeDeviceId(String deviceId) {
        persistentStorage.put(DEVICE_ID, deviceId);
    }

    public void storeDeviceName(String deviceName) {
        persistentStorage.put(DEVICE_NAME, deviceName);
    }

    public String getToken() {
        return sessionStorage.get(TOKEN);
    }

    public void storeBatchInstallationId(String batchInstallationId) {
        persistentStorage.put(BATCH_INSTALLATION_ID, batchInstallationId);
    }

    public String getBatchInstallationId() {
        return persistentStorage.get(BATCH_INSTALLATION_ID);
    }

    public void clear() {
        persistentStorage.clear();
        sessionStorage.clear();
    }

    private void storeKeyPair(KeyPair keyPair, String publicKeyName, String privateKeyName) {
        String encodedPublicKey =
                EncodingUtils.encodeAsBase64String(keyPair.getPublic().getEncoded());
        String encodedPrivateKey =
                EncodingUtils.encodeAsBase64String(keyPair.getPrivate().getEncoded());
        persistentStorage.put(publicKeyName, encodedPublicKey);
        persistentStorage.put(privateKeyName, encodedPrivateKey);
    }

    private KeyPair getECKeyPair(String publicKeyName, String privateKeyName) {
        String encodedPublicKey = persistentStorage.get(publicKeyName);
        String encodedPrivateKey = persistentStorage.get(privateKeyName);
        return AxaCryptoUtil.generateKeyPairFromBase64(encodedPublicKey, encodedPrivateKey);
    }
}
