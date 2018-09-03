package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcDevice;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.ActivationLicenseResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.EnrollDeviceRoundTwoResponse;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

public class KbcAuthenticator implements MultiFactorAuthenticator, AutoAuthenticator {

    private final Catalog catalog;
    private final PersistentStorage persistentStorage;
    private final KbcApiClient apiClient;
    private final SupplementalInformationController supplementalInformationController;

    public KbcAuthenticator(Catalog catalog, PersistentStorage persistentStorage, KbcApiClient apiClient,
            SupplementalInformationController supplementalInformationController) {
        this.catalog = catalog;
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
        this.supplementalInformationController = supplementalInformationController;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {
        String panNr = credentials.getField(Field.Key.USERNAME);

        if (Strings.isNullOrEmpty(panNr)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        apiClient.prepareSession();
        registerLogon(panNr);

        String signingId = apiClient.enrollDevice();
        String signTypeId = apiClient.signTypeManual(signingId);
        String signChallengeCode = apiClient.signChallenge(signTypeId, signingId);
        String signResponseCode = waitForSignCode(signChallengeCode);
        String finalSigningId = apiClient.signValidation(signResponseCode, panNr, signingId);
        EnrollDeviceRoundTwoResponse enrollDeviceRoundTwoResponse = enrollDeviceRoundTwo(finalSigningId);

        KbcDevice device = createAndActivateKbcDevice(enrollDeviceRoundTwoResponse);

        apiClient.logout();
        
        login(device);
    }

    private void registerLogon(String panNr) throws AuthenticationException {
        String challengeCode = apiClient.challenge();
        String responseCode = waitForLoginCode(challengeCode);

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

    private EnrollDeviceRoundTwoResponse enrollDeviceRoundTwo(String finalSigningId) throws AuthenticationException {
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

    private KbcDevice createAndActivateKbcDevice(EnrollDeviceRoundTwoResponse enrollDeviceRoundTwoResponse) {
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
        return matchesErrorMessage(e, KbcConstants.ErrorMessage.INCORRECT_CARD_NUMBER);
    }

    private boolean isNotACustomer(IllegalStateException e) {
        return matchesErrorMessage(e, KbcConstants.ErrorMessage.NOT_A_CUSTOMER);
    }

    private boolean isIncorrectLoginCode(IllegalStateException e) {
        return matchesErrorMessage(e, KbcConstants.ErrorMessage.INCORRECT_LOGIN_CODE);
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

    private String waitForLoginCode(String challenge) throws SupplementalInfoException {
    return waitForSupplementalInformation(
        createDescriptionField(catalog.getString(
                "1$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_CardReader.png)Insert your bank card into the card reader\n"
                + "2$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_LOGIN.png) ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_LOGIN.png) Tap\n"
                + "3$  Enter the start code []"),
            challenge),
        createInputField(catalog.getString(
                "4$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_OK.png) Tap\n"
                    + "5$  Enter your secret code\n"
                    + "6$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_OK.png) Tap\n"
                    + "7$  Enter the login code")));
    }

    private String waitForSignCode(String challenge) throws SupplementalInfoException {
    return waitForSupplementalInformation(
        createDescriptionField(catalog.getString(
            "1$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_CardReader.png) Insert your bank card into the card reader\n"
                + "2$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_SIGN.png) ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_SIGN.png) Tap\n"
                + "3$  Enter the start code []"),
            challenge),
        createInputField(catalog.getString(
            "4$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_OK.png) ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_OK.png) Tap\n"
                    + "5$  Enter your PIN\n"
                    + "6$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_OK.png) Tap\n"
                    + "7$  Enter the sign code")));
    }

    private String waitForSupplementalInformation(Field... fields)
            throws SupplementalInfoException {
        return supplementalInformationController.askSupplementalInformation(fields)
                .get(KbcConstants.MultiFactorAuthentication.CODE);
    }

    private Field createDescriptionField(String helpText, String challenge) {
        String formattedChallenge = getChallengeFormattedWithSpace(challenge);
        Field field = new Field();
        field.setMasked(false);
        field.setDescription(formattedChallenge);
        field.setValue(formattedChallenge);
        field.setName("description");
        field.setHelpText(helpText);
        field.setImmutable(true);
        return field;
    }

    private Field createInputField(String helpText) {
        Field field = new Field();
        field.setMasked(false);
        field.setDescription(catalog.getString("Input"));
        field.setName(KbcConstants.MultiFactorAuthentication.CODE);
        field.setHelpText(helpText);
        field.setNumeric(true);
        field.setHint("NNNNNNN");
        return field;
    }

    private String getChallengeFormattedWithSpace(String challenge) {
        if (challenge.length() != 8) {
            // We expect the challenge to consist of 8 numbers, if not we don't try to format for readability
            return challenge;
        }

        return String.format("%s %s",
                challenge.substring(0, 4),
                challenge.substring(4));
    }
}
