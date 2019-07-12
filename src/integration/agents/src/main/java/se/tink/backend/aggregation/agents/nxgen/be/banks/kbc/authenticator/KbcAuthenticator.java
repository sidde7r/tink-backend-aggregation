package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator;

import com.google.common.base.Strings;
import java.util.Arrays;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
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
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class KbcAuthenticator
        implements MultiFactorAuthenticator, AutoAuthenticator, ProgressiveAuthenticator {

    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final KbcApiClient apiClient;
    private final SupplementalInformationFormer supplementalInformationFormer;
    private static final AggregationLogger logger = new AggregationLogger(KbcAuthenticator.class);

    public KbcAuthenticator(
            final SessionStorage sessionStorage,
            final PersistentStorage persistentStorage,
            final KbcApiClient apiClient,
            final SupplementalInformationFormer supplementalInformationFormer) {
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
        this.supplementalInformationFormer = supplementalInformationFormer;
    }

    @Override
    public Iterable<? extends AuthenticationStep> authenticationSteps(
            final Credentials credentials) {
        return Arrays.asList(
                new LoginStep(this, sessionStorage, apiClient, supplementalInformationFormer),
                new SignStep(this, sessionStorage, apiClient, supplementalInformationFormer),
                new FinalStep(this, sessionStorage, apiClient));
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    static byte[] generateCipherKey() {
        return RandomUtils.secureRandom(KbcConstants.Encryption.AES_KEY_LENGTH);
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        throw new AssertionError();
    }

    String verifyCredentialsNotNullOrEmpty(String value) throws LoginException {
        if (Strings.isNullOrEmpty(value) || value.trim().isEmpty()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        return value;
    }

    EnrollDeviceRoundTwoResponse enrollDeviceRoundTwo(String finalSigningId, final byte[] cipherKey)
            throws AuthenticationException, AuthorizationException {
        try {
            return apiClient.enrollDeviceWithSigningId(finalSigningId, cipherKey);
        } catch (IllegalStateException e) {
            if (isNotACustomer(e)) {
                throw LoginError.NOT_CUSTOMER.exception(
                        KbcConstants.UserMessage.NOT_A_CUSTOMER.getKey());
            }

            if (isIncorrectSignCode(e)) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            }

            throw e;
        }
    }

    KbcDevice createAndActivateKbcDevice(
            EnrollDeviceRoundTwoResponse enrollDeviceRoundTwoResponse, final byte[] cipherKey)
            throws AuthorizationException {
        KbcDevice device = new KbcDevice();
        device.setDeviceId(enrollDeviceRoundTwoResponse.getDeviceId().getValue());
        device.setAccessNumber(enrollDeviceRoundTwoResponse.getAccessNumber().getValue());

        byte[] aesKey0 =
                KbcEnryptionUtils.deriveKey(
                        enrollDeviceRoundTwoResponse.getActivationPassword().getValue());
        byte[] iv = KbcDevice.generateIv();
        String encryptedClientPublicKeyAndNonce =
                device.encryptClientPublicKeyAndNonce(aesKey0, iv);

        ActivationLicenseResponse activationLicenseResponse =
                apiClient.activationLicence(
                        device,
                        EncodingUtils.encodeHexAsString(iv).toUpperCase(),
                        encryptedClientPublicKeyAndNonce,
                        cipherKey);

        byte[] serverPublicKey =
                KbcEnryptionUtils.decryptServerPublicKey(aesKey0, activationLicenseResponse);
        byte[] sharedSecret = device.calculateSharedSecret(serverPublicKey);
        byte[] staticVector =
                KbcEnryptionUtils.decryptStaticVector(sharedSecret, activationLicenseResponse);
        byte[] dynamicVector =
                KbcEnryptionUtils.decryptDynamicVector(sharedSecret, activationLicenseResponse);

        device.setStaticVector(new String(staticVector));
        device.setDynamicVector(new String(dynamicVector));

        String challenge = activationLicenseResponse.getChallenge().getValue();
        String deviceCode = device.calculateDeviceCode(challenge);

        iv = KbcDevice.generateIv();
        String encryptedNonce =
                KbcEnryptionUtils.decryptAndEncryptNonce(
                        sharedSecret, iv, activationLicenseResponse);

        String activationMessage =
                apiClient.activationInstance(
                        device,
                        EncodingUtils.encodeHexAsString(iv).toUpperCase(),
                        encryptedNonce,
                        challenge,
                        deviceCode,
                        cipherKey);

        device.setActivationMessage(activationMessage);
        String verificationMessage = device.calculateVerificationMessage();

        apiClient.activationVerification(device, verificationMessage, cipherKey);

        return device;
    }

    boolean isIncorrectCardNumber(IllegalStateException e) {
        if (matchesErrorMessage(e, KbcConstants.HeaderErrorMessage.INCORRECT_CARD_NUMBER)) {
            return true;
        }
        return possibleUnhandledErrorCodeLogAndCheckTextMessage(
                e, KbcConstants.ErrorMessage.INCORRECT_CARD_NUMBER);
    }

    boolean isIncorrectCard(IllegalStateException e) {
        return matchesErrorMessage(
                e, KbcConstants.HeaderErrorMessage.CANNOT_LOGIN_USING_THIS_CARD_CONTACT_KBC);
    }

    private boolean possibleUnhandledErrorCodeLogAndCheckTextMessage(
            IllegalStateException e, String textMessage) {
        logger.warnExtraLong(
                String.format("Error message: %s", e.getMessage()),
                KbcConstants.LogTags.ERROR_CODE_MESSAGE);
        return matchesErrorMessage(e, textMessage);
    }

    private boolean isNotACustomer(IllegalStateException e) {
        return possibleUnhandledErrorCodeLogAndCheckTextMessage(
                e, KbcConstants.ErrorMessage.NOT_A_CUSTOMER);
    }

    boolean isIncorrectLoginCodeLastAttempt(IllegalStateException e) {
        return matchesErrorMessage(
                e, KbcConstants.HeaderErrorMessage.INCORRECT_LOGIN_CODE_ONE_ATTEMPT_LEFT);
    }

    boolean isIncorrectLoginCode(IllegalStateException e) {
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
        final byte[] cipherKey = generateCipherKey();
        sessionStorage.put(
                KbcConstants.Encryption.AES_SESSION_KEY_KEY,
                EncodingUtils.encodeAsBase64String(cipherKey));
        apiClient.prepareSession(cipherKey);

        String otpChallenge = apiClient.challengeSotp(device, cipherKey);

        String otp = device.calculateAuthenticationOtp(otpChallenge);

        apiClient.loginSotp(device, otp, cipherKey);

        persistentStorage.put(KbcConstants.Storage.DEVICE_KEY, device);
    }
}
