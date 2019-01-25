package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcDevice;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.ActivationLicenseResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.EnrollDeviceRoundTwoResponse;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.libraries.i18n.LocalizableKey;

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
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {
        String panNr = verifyCredentialsNotNullOrEmpty(credentials.getField(Field.Key.USERNAME));
        apiClient.prepareSession();
        registerLogon(panNr);

        String signingId = apiClient.enrollDevice();
        String signTypeId = apiClient.signTypeManual(signingId);
        String signChallengeCode = apiClient.signChallenge(signTypeId, signingId);
        String signResponseCode = verifyCredentialsNotNullOrEmpty(
                supplementalInformationHelper.waitForSignCodeChallengeResponse(signChallengeCode));
        String finalSigningId = apiClient.signValidation(signResponseCode, panNr, signingId);
        EnrollDeviceRoundTwoResponse enrollDeviceRoundTwoResponse = enrollDeviceRoundTwo(finalSigningId);

        KbcDevice device = createAndActivateKbcDevice(enrollDeviceRoundTwoResponse);

        apiClient.logout();
        
        login(device);
    }

    private String verifyCredentialsNotNullOrEmpty(String value) throws LoginException {
        if (Strings.isNullOrEmpty(value) || value.trim().isEmpty()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        return value;
    }

    private void registerLogon(String panNr) throws AuthenticationException, AuthorizationException {
        String challengeCode = apiClient.challenge();
        String responseCode =verifyCredentialsNotNullOrEmpty(
                supplementalInformationHelper.waitForLoginChallengeResponse(challengeCode));
        try {
            apiClient.registerLogon(panNr, responseCode);
        } catch (IllegalStateException e) {
            if (isIncorrectCardNumber(e)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(
                        KbcConstants.UserMessage.INCORRECT_CARD_NUMBER.getKey());
            }

            if (isIncorrectLoginCode(e)) {
                // Using the exact message from bank since it contains info about number of remaining attempts.
                throw LoginError.INCORRECT_CREDENTIALS.exception(new LocalizableKey(
                        StringUtils.substringAfterLast(e.getMessage(), "[Message]: ")));
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
                throw LoginError.NOT_CUSTOMER.exception(KbcConstants.UserMessage.NOT_A_CUSTOMER.getKey());
            }

            if (isIncorrectSignCode(e)) {
                // Using the exact message from bank since it contains info about number of remaining attempts.
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(new LocalizableKey(
                        StringUtils.substringAfterLast(e.getMessage(), "[Message]: ")));
            }

            throw e;
        }
    }

    private KbcDevice createAndActivateKbcDevice(EnrollDeviceRoundTwoResponse enrollDeviceRoundTwoResponse)
            throws AuthorizationException {
        KbcDevice device = new KbcDevice();
        device.setDeviceId(enrollDeviceRoundTwoResponse.getDeviceId().getValue());
        device.setAccessNumber(enrollDeviceRoundTwoResponse.getAccessNumber().getValue());

        byte[] aesKey0 = KbcEnryptionUtils.deriveKey(enrollDeviceRoundTwoResponse.getActivationPassword().getValue());
        byte[] iv = KbcDevice.generateIv();
        String encryptedClientPublicKeyAndNonce = device.encryptClientPublicKeyAndNonce(aesKey0, iv);

        ActivationLicenseResponse activationLicenseResponse = apiClient.activationLicence(
                device, EncodingUtils.encodeHexAsString(iv).toUpperCase(), encryptedClientPublicKeyAndNonce);

        byte[] serverPublicKey = KbcEnryptionUtils.decryptServerPublicKey(aesKey0, activationLicenseResponse);
        byte[] sharedSecret = device.calculateSharedSecret(serverPublicKey);
        byte[] staticVector = KbcEnryptionUtils.decryptStaticVector(sharedSecret, activationLicenseResponse);
        byte[] dynamicVector = KbcEnryptionUtils.decryptDynamicVector(sharedSecret, activationLicenseResponse);

        device.setStaticVector(new String(staticVector));
        device.setDynamicVector(new String(dynamicVector));

        String challenge = activationLicenseResponse.getChallenge().getValue();
        String deviceCode = device.calculateDeviceCode(challenge);

        iv = KbcDevice.generateIv();
        String encryptedNonce = KbcEnryptionUtils.decryptAndEncryptNonce(sharedSecret, iv, activationLicenseResponse);

        String activationMessage = apiClient.activationInstance(
                device,
                EncodingUtils.encodeHexAsString(iv).toUpperCase(),
                encryptedNonce,
                challenge,
                deviceCode);

        device.setActivationMessage(activationMessage);
        String verificationMessage = device.calculateVerificationMessage();

        apiClient.activationVerification(device, verificationMessage);

        return device;
    }
    
    private boolean isIncorrectCardNumber(IllegalStateException e) {
        if (matchesErrorMessage(e, KbcConstants.HeaderErrorMessage.INCORRECT_CARD_NUMBER)) {
            return true;
        }
        return possibleUnhandledErrorCodeLogAndCheckTextMessage(e, KbcConstants.ErrorMessage.INCORRECT_CARD_NUMBER);
    }

    private boolean possibleUnhandledErrorCodeLogAndCheckTextMessage(IllegalStateException e, String textMessage) {
        LOGGER.warnExtraLong(String.format("Error message: %s", e.getMessage()), KbcConstants.LogTags.ERROR_CODE_MESSAGE);
        return matchesErrorMessage(e, textMessage);
    }

    private boolean isNotACustomer(IllegalStateException e) {
        return possibleUnhandledErrorCodeLogAndCheckTextMessage(e, KbcConstants.ErrorMessage.NOT_A_CUSTOMER);
    }

    private boolean isIncorrectLoginCode(IllegalStateException e) {
        if (matchesErrorMessage(e, KbcConstants.HeaderErrorMessage.INCORRECT_LOGIN_CODE_ONE_ATTEMPT_LEFT)
            || matchesErrorMessage(e, KbcConstants.HeaderErrorMessage.INCORRECT_LOGIN_CODE_TWO_ATTEMPT_LEFT)) {
            return true;
        }
        return possibleUnhandledErrorCodeLogAndCheckTextMessage(e, KbcConstants.ErrorMessage.INCORRECT_LOGIN_CODE);
    }

    private boolean isIncorrectSignCode(IllegalStateException e) {
        return matchesErrorMessage(e, KbcConstants.ErrorMessage.INCORRECT_SIGN_CODE);
    }

    private boolean matchesErrorMessage(IllegalStateException e, String errorMessage) {
        return e.getMessage() != null &&
                e.getMessage().toLowerCase().contains(errorMessage);
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        KbcDevice device = persistentStorage.get(KbcConstants.Storage.DEVICE_KEY, KbcDevice.class).orElseThrow(
                () -> new IllegalStateException("Device data not found"));
        try {
            login(device);
        } catch (AuthenticationException | AuthorizationException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    public void login(KbcDevice device) throws AuthenticationException, AuthorizationException {
        apiClient.prepareSession();

        String otpChallenge = apiClient.challengeSotp(device);

        String otp = device.calculateAuthenticationOtp(otpChallenge);

        apiClient.loginSotp(device, otp);

        persistentStorage.put(KbcConstants.Storage.DEVICE_KEY, device);
    }
}
