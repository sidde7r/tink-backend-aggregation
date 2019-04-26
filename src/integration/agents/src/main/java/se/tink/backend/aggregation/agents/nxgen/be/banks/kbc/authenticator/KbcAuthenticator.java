package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator;

import com.google.common.base.Strings;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants.LogTags;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcDevice;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.ActivationLicenseResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.EnrollDeviceRoundTwoResponse;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class KbcAuthenticator implements MultiFactorAuthenticator, AutoAuthenticator {

    private final PersistentStorage persistentStorage;
    private final KbcApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private static final AggregationLogger LOGGER = new AggregationLogger(KbcAuthenticator.class);

    public KbcAuthenticator(
            final PersistentStorage persistentStorage,
            final KbcApiClient apiClient,
            final SupplementalInformationHelper supplementalInformationHelper) {
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String panNr = verifyCredentialsNotNullOrEmpty(credentials.getField(Field.Key.USERNAME));
        apiClient.prepareSession();
        registerLogon(panNr);

        String signingId = apiClient.enrollDevice();
        String signTypeId = apiClient.signTypeManual(signingId);
        String signChallengeCode = apiClient.signChallenge(signTypeId, signingId);
        LOGGER.info(
                String.format(
                        "%s KBC waitForSignCodeChallengeResponse, %s",
                        LogTags.DEBUG, signChallengeCode));
        String signResponseCode =
                verifyCredentialsNotNullOrEmpty(
                        supplementalInformationHelper.waitForSignCodeChallengeResponse(
                                signChallengeCode));
        LOGGER.info(
                String.format(
                        "%s signValidation(%s, %s, %s)",
                        LogTags.DEBUG, signResponseCode, panNr, signingId));
        String finalSigningId = apiClient.signValidation(signResponseCode, panNr, signingId);
        LOGGER.info(String.format("%s enrollDeviceRoundTwo(%s)", LogTags.DEBUG, finalSigningId));
        EnrollDeviceRoundTwoResponse enrollDeviceRoundTwoResponse =
                enrollDeviceRoundTwo(finalSigningId);

        LOGGER.info(String.format("%s createAndActivateKbcDevice", LogTags.DEBUG));
        KbcDevice device = createAndActivateKbcDevice(enrollDeviceRoundTwoResponse);

        LOGGER.info(String.format("%s apiClient.logout", LogTags.DEBUG));
        apiClient.logout();

        LOGGER.info(String.format("%s login(device)", LogTags.DEBUG));
        login(device);
    }

    private String verifyCredentialsNotNullOrEmpty(String value) throws LoginException {
        if (Strings.isNullOrEmpty(value) || value.trim().isEmpty()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        return value;
    }

    private void registerLogon(String panNr)
            throws AuthenticationException, AuthorizationException {
        String challengeCode = apiClient.challenge();
        LOGGER.info(
                String.format(
                        "%s KBC waitForLoginChallengeResponse, %s", LogTags.DEBUG, challengeCode));
        String responseCode =
                verifyCredentialsNotNullOrEmpty(
                        supplementalInformationHelper.waitForLoginChallengeResponse(challengeCode));
        LOGGER.info(String.format("%s KBC waitForLoginChallengeResponse done", LogTags.DEBUG));
        try {
            apiClient.registerLogon(panNr, responseCode);
        } catch (IllegalStateException e) {
            if (isIncorrectCardNumber(e) || isIncorrectCard(e)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(
                        KbcConstants.UserMessage.INCORRECT_CARD_NUMBER.getKey());
            }

            if (isIncorrectLoginCodeLastAttempt(e)) {
                throw LoginError.INCORRECT_CREDENTIALS_LAST_ATTEMPT.exception();
            }

            if (isIncorrectLoginCode(e)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            throw e;
        }
    }

    private EnrollDeviceRoundTwoResponse enrollDeviceRoundTwo(String finalSigningId)
            throws AuthenticationException, AuthorizationException {
        try {
            return apiClient.enrollDeviceWithSigningId(finalSigningId);
        } catch (IllegalStateException e) {
            if (isNotACustomer(e)) {
                LOGGER.info(String.format("%s enrollDeviceRoundTwo isNotACustomer", LogTags.DEBUG));
                throw LoginError.NOT_CUSTOMER.exception(
                        KbcConstants.UserMessage.NOT_A_CUSTOMER.getKey());
            }

            if (isIncorrectSignCode(e)) {
                LOGGER.info(
                        String.format(
                                "%s enrollDeviceRoundTwo isIncorrectSignCode", LogTags.DEBUG));
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            }

            LOGGER.info(String.format("%s enrollDeviceRoundTwo throw", LogTags.DEBUG));
            throw e;
        }
    }

    private KbcDevice createAndActivateKbcDevice(
            EnrollDeviceRoundTwoResponse enrollDeviceRoundTwoResponse)
            throws AuthorizationException {
        KbcDevice device = new KbcDevice();
        device.setDeviceId(enrollDeviceRoundTwoResponse.getDeviceId().getValue());
        device.setAccessNumber(enrollDeviceRoundTwoResponse.getAccessNumber().getValue());

        LOGGER.info(String.format("%s createAndActivateKbcDevice deriveKey", LogTags.DEBUG));
        byte[] aesKey0 =
                KbcEnryptionUtils.deriveKey(
                        enrollDeviceRoundTwoResponse.getActivationPassword().getValue());
        byte[] iv = KbcDevice.generateIv();
        String encryptedClientPublicKeyAndNonce =
                device.encryptClientPublicKeyAndNonce(aesKey0, iv);

        LOGGER.info(
                String.format("%s createAndActivateKbcDevice activationLicense", LogTags.DEBUG));
        ActivationLicenseResponse activationLicenseResponse =
                apiClient.activationLicence(
                        device,
                        EncodingUtils.encodeHexAsString(iv).toUpperCase(),
                        encryptedClientPublicKeyAndNonce);

        LOGGER.info(
                String.format(
                        "%s createAndActivateKbcDevice decryptServerPublicKey(byte[%d], %s)",
                        LogTags.DEBUG,
                        aesKey0.length,
                        SerializationUtils.serializeToString(activationLicenseResponse)));
        byte[] serverPublicKey =
                KbcEnryptionUtils.decryptServerPublicKey(aesKey0, activationLicenseResponse);
        LOGGER.info(
                String.format(
                        "%s calculateSharedSecret(byte[%d])",
                        LogTags.DEBUG, serverPublicKey.length));
        byte[] sharedSecret = device.calculateSharedSecret(serverPublicKey);
        LOGGER.info(
                String.format(
                        "%s decryptStaticVector(byte[%d], activationLicenseResponse)",
                        LogTags.DEBUG, sharedSecret.length));
        byte[] staticVector =
                KbcEnryptionUtils.decryptStaticVector(sharedSecret, activationLicenseResponse);
        LOGGER.info(
                String.format(
                        "%s decryptDynamicVector(byte[%d], activationLicenseResponse)",
                        LogTags.DEBUG, sharedSecret.length));
        byte[] dynamicVector =
                KbcEnryptionUtils.decryptDynamicVector(sharedSecret, activationLicenseResponse);

        LOGGER.info(String.format("%s setStaticVector", LogTags.DEBUG));
        device.setStaticVector(new String(staticVector));
        LOGGER.info(String.format("%s setDynamicVector", LogTags.DEBUG));
        device.setDynamicVector(new String(dynamicVector));
        LOGGER.info(String.format("%s setDynamicVector done", LogTags.DEBUG));

        String challenge = activationLicenseResponse.getChallenge().getValue();
        LOGGER.info(String.format("%s calculateDeviceCode(%s)", LogTags.DEBUG, challenge));
        String deviceCode = device.calculateDeviceCode(challenge);

        LOGGER.info(String.format("%s createAndActivateKbcDevice generateIv", LogTags.DEBUG));
        iv = KbcDevice.generateIv();
        LOGGER.info(
                String.format(
                        "%s createAndActivateKbcDevice decryptAndEncryptNonce", LogTags.DEBUG));
        String encryptedNonce =
                KbcEnryptionUtils.decryptAndEncryptNonce(
                        sharedSecret, iv, activationLicenseResponse);

        LOGGER.info(
                String.format(
                        "%s createAndActivateKbcDevice apiClient.activationInstance",
                        LogTags.DEBUG));
        String activationMessage =
                apiClient.activationInstance(
                        device,
                        EncodingUtils.encodeHexAsString(iv).toUpperCase(),
                        encryptedNonce,
                        challenge,
                        deviceCode);

        device.setActivationMessage(activationMessage);
        LOGGER.info(
                String.format(
                        "%s createAndActivateKbcDevice calculateVerificationMessage",
                        LogTags.DEBUG));
        String verificationMessage = device.calculateVerificationMessage();

        LOGGER.info(
                String.format(
                        "%s createAndActivateKbcDevice activationVerification", LogTags.DEBUG));
        apiClient.activationVerification(device, verificationMessage);

        LOGGER.info(String.format("%s createAndActivateKbcDevice return", LogTags.DEBUG));
        return device;
    }

    private boolean isIncorrectCardNumber(IllegalStateException e) {
        if (matchesErrorMessage(e, KbcConstants.HeaderErrorMessage.INCORRECT_CARD_NUMBER)) {
            return true;
        }
        return possibleUnhandledErrorCodeLogAndCheckTextMessage(
                e, KbcConstants.ErrorMessage.INCORRECT_CARD_NUMBER);
    }

    private boolean isIncorrectCard(IllegalStateException e) {
        return matchesErrorMessage(
                e, KbcConstants.HeaderErrorMessage.CANNOT_LOGIN_USING_THIS_CARD_CONTACT_KBC);
    }

    private boolean possibleUnhandledErrorCodeLogAndCheckTextMessage(
            IllegalStateException e, String textMessage) {
        LOGGER.warnExtraLong(
                String.format("Error message: %s", e.getMessage()),
                KbcConstants.LogTags.ERROR_CODE_MESSAGE);
        return matchesErrorMessage(e, textMessage);
    }

    private boolean isNotACustomer(IllegalStateException e) {
        return possibleUnhandledErrorCodeLogAndCheckTextMessage(
                e, KbcConstants.ErrorMessage.NOT_A_CUSTOMER);
    }

    private boolean isIncorrectLoginCodeLastAttempt(IllegalStateException e) {
        return matchesErrorMessage(
                e, KbcConstants.HeaderErrorMessage.INCORRECT_LOGIN_CODE_ONE_ATTEMPT_LEFT);
    }

    private boolean isIncorrectLoginCode(IllegalStateException e) {
        if (matchesErrorMessage(
                e, KbcConstants.HeaderErrorMessage.INCORRECT_LOGIN_CODE_TWO_ATTEMPT_LEFT)) {
            return true;
        }
        return possibleUnhandledErrorCodeLogAndCheckTextMessage(
                e, KbcConstants.ErrorMessage.INCORRECT_LOGIN_CODE);
    }

    private boolean isIncorrectSignCode(IllegalStateException e) {
        return possibleUnhandledErrorCodeLogAndCheckTextMessage(
                e, KbcConstants.ErrorMessage.INCORRECT_SIGN_CODE);
    }

    private boolean matchesErrorMessage(IllegalStateException e, String errorMessage) {
        return e.getMessage() != null && e.getMessage().toLowerCase().contains(errorMessage);
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        KbcDevice device =
                persistentStorage
                        .get(KbcConstants.Storage.DEVICE_KEY, KbcDevice.class)
                        .orElseThrow(() -> new IllegalStateException("Device data not found"));
        try {
            login(device);
        } catch (AuthenticationException | AuthorizationException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    public void login(KbcDevice device) throws AuthenticationException, AuthorizationException {
        LOGGER.info(String.format("%s apiclient.prepareSession", LogTags.DEBUG));
        apiClient.prepareSession();

        LOGGER.info(String.format("%s apiclient.challengeSotp", LogTags.DEBUG));
        String otpChallenge = apiClient.challengeSotp(device);

        LOGGER.info(
                String.format("%s calculateAuthenticationOtp, %s", LogTags.DEBUG, otpChallenge));
        String otp = device.calculateAuthenticationOtp(otpChallenge);

        LOGGER.info(String.format("%s loginSotp, %s", LogTags.DEBUG, otp));
        apiClient.loginSotp(device, otp);

        persistentStorage.put(KbcConstants.Storage.DEVICE_KEY, device);
    }
}
