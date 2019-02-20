package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator;

import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngCryptoUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.controller.ChallengeExchangeValues;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.CryptoInitValues;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.InitEnrollResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.LoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.MobileHelloResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.PrepareEnrollResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.AppCredentialsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.InitEnrollResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.BaseMobileResponseEntity;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.LocalizableKey;

public class IngCardReaderAuthenticator {
    private static final AggregationLogger LOGGER = new AggregationLogger(IngCardReaderAuthenticator.class);

    private final IngApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final IngHelper ingHelper;
    private final CryptoInitValues cryptoInitValues;

    public IngCardReaderAuthenticator(IngApiClient apiClient, PersistentStorage persistentStorage,
            IngHelper ingHelper) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.ingHelper = ingHelper;
        this.cryptoInitValues = new CryptoInitValues();
    }

    public ChallengeExchangeValues initEnroll(String ingId, String cardNumber, String otp)
            throws AuthenticationException, AuthorizationException {
        // Get cookies and authentication urls
        MobileHelloResponseEntity mobileHelloResponseEntity = this.apiClient.mobileHello();
        this.ingHelper.addRequestUrls(mobileHelloResponseEntity.getRequests());

        // Send authentication values
        String authUrl = this.ingHelper.getUrl(IngConstants.RequestNames.AUTHENTICATE);
        this.apiClient.trustBuilderEnroll(authUrl, ingId, cardNumber, otp, this.cryptoInitValues.getDeviceId());

        // Initiate enrollment of new device
        String initEnrollUrl = this.ingHelper.getUrl(IngConstants.RequestNames.ENROL_DEVICE);
        HttpResponse initEnrollHttpResponse =
                this.apiClient.initEnroll(initEnrollUrl, ingId, cardNumber, this.cryptoInitValues.getDeviceId());
        checkForUserInputError(initEnrollHttpResponse);

        InitEnrollResponse initEnrollResponse = initEnrollHttpResponse.getBody(InitEnrollResponse.class);
        validateResponseHeader(initEnrollResponse.getMobileResponse());
        InitEnrollResponseEntity initEnrollResponseEntity = initEnrollResponse.getMobileResponse();
        this.ingHelper.addRequestUrls(initEnrollResponseEntity.getRequests());

        // Calculate and save values that are needed for auto authentication
        calcAndPersistAuthValues(ingId, initEnrollResponseEntity);

        // Get challenge and signingId needed for confirming enrollment
        String prepEnrollUrl = this.ingHelper.getUrl(IngConstants.RequestNames.PREPARE_ENROLL);
        PrepareEnrollResponseEntity prepEnrollResponseEntity = this.apiClient.prepareEnroll(prepEnrollUrl);

        return new ChallengeExchangeValues(
                Preconditions.checkNotNull(prepEnrollResponseEntity.getChallenges().get(0)),
                prepEnrollResponseEntity.getSigningId());
    }

    public void confirmEnroll(String ingId, String challengeResponse, String signingId) throws LoginException {
        int otpCounter = 1;
        int otpSystem = calcOtp(otpCounter);

        String confirmEnrollUrl = this.ingHelper.getUrl(IngConstants.RequestNames.CONFIRM_ENROLL);

        BaseMobileResponseEntity confirmEnrollResponseEntity = this.apiClient.confirmEnroll(
                confirmEnrollUrl,
                ingId,
                signingId,
                challengeResponse,
                otpSystem,
                this.cryptoInitValues.getDeviceId());

        checkForChallengeExchangeError(confirmEnrollResponseEntity);

        String logoutUrl = this.ingHelper.getUrl(IngConstants.RequestNames.LOGOUT);
        this.apiClient.logout(logoutUrl);
    }

    public void authenticate(String ingId) throws LoginException {
        int otpCounter = Integer.parseInt(this.persistentStorage.get(IngConstants.Storage.OTP_COUNTER));
        int otp = calcOtp(otpCounter);

        String authUrl = this.ingHelper.getUrl(IngConstants.RequestNames.AUTHENTICATE);

        this.apiClient.trustBuilderLogin(
                authUrl,
                ingId,
                this.persistentStorage.get(IngConstants.Storage.VIRTUAL_CARDNUMBER),
                otp,
                this.cryptoInitValues.getDeviceId(),
                this.persistentStorage.get(IngConstants.Storage.PSN));

        String loginUrl = this.ingHelper.getUrl(IngConstants.RequestNames.LOGON);

        LoginResponseEntity loginResponseEntity = this.apiClient.login(
                loginUrl,
                ingId,
                this.persistentStorage.get(IngConstants.Storage.VIRTUAL_CARDNUMBER),
                this.cryptoInitValues.getDeviceId());

        validateResponseHeader(loginResponseEntity);
        this.ingHelper.persist(loginResponseEntity);
    }

    private void validateResponseHeader(BaseMobileResponseEntity responseEntity) throws LoginException {
        if (IngConstants.ReturnCodes.NOK.equalsIgnoreCase(responseEntity.getReturnCode())) {
            String errorCode = responseEntity.getErrorCode().orElse("No error code");
            if (IngConstants.ErrorCodes.NO_ACCESS_TO_ONLINE_BANKING.equalsIgnoreCase(errorCode)
                    || IngConstants.ErrorCodes.NO_LINKED_ACCOUNT.equalsIgnoreCase(errorCode)) {
                throw LoginError.REGISTER_DEVICE_ERROR.exception();
            }
            if(IngConstants.ErrorCodes.ACCOUNT_CANCELLED.equalsIgnoreCase(errorCode)) {
                throw LoginError.NOT_CUSTOMER.exception();
            }
            String errormsg =
                    String.format(
                            "%s%s%s%s",
                            "Error during authenticate! Code: ",
                            errorCode,
                            " Message: ",
                            responseEntity.getErrorText().get());
            LOGGER.errorExtraLong(
                    errormsg,
                    IngConstants.Logs.UNKNOWN_ERROR_CODE,
                    new IllegalStateException("Error during autoAuth!"));
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
        }
    }

    private void calcAndPersistAuthValues(String ingId, InitEnrollResponseEntity initEnrollResponseEntity) {
        String regCode = initEnrollResponseEntity.getRegistrationCode();
        String pin = initEnrollResponseEntity.getPin();
        String virtualCardNumber = initEnrollResponseEntity.getVirtualCardNumber();

        byte[] credentials = executeGetAppCredentials(regCode, this.cryptoInitValues.getSessionKey(),
                this.cryptoInitValues.getSessionKeyAuth());

        byte[] secret0 = Arrays.copyOfRange(credentials, 5, 21);
        byte[] secret1 = Arrays.copyOfRange(credentials, 21, 37);
        byte[] otpKey = IngCryptoUtils.deriveOtpKey(pin.getBytes(), secret0, secret1);

        persistAuthValues(this.cryptoInitValues, ingId, virtualCardNumber, otpKey, pin, secret0, secret1);
    }

    private void persistAuthValues(CryptoInitValues cryptoInitValues, String ingId,
            String compositeVirtualCardNumber, byte[] otpKey, String systemPin, byte[] secret0, byte[] secret1) {
        String[] split = compositeVirtualCardNumber.split(":");
        String virtualCardNumber = split[0];
        String psn = split[1];

        LOGGER.debug("Checking OTP HEX value to save to Persistent Storage: " +  EncodingUtils.encodeHexAsString
                (otpKey));
        LOGGER.debug("ING values to save to Persistent Storage: " +  ingId);

        this.persistentStorage.put(IngConstants.Storage.ING_ID, ingId);
        this.persistentStorage.put(IngConstants.Storage.DEVICE_ID, cryptoInitValues.getDeviceId());
        this.persistentStorage.put(IngConstants.Storage.VIRTUAL_CARDNUMBER, virtualCardNumber);
        this.persistentStorage.put(IngConstants.Storage.PSN, psn);
        this.persistentStorage.put(IngConstants.Storage.OTP_KEY_HEX, EncodingUtils.encodeHexAsString(otpKey));
        this.persistentStorage.put(IngConstants.Storage.SYSTEM_PIN, systemPin);
        this.persistentStorage.put(IngConstants.Storage.SECRET_0_IN_HEX, EncodingUtils.encodeHexAsString(secret0));
        this.persistentStorage.put(IngConstants.Storage.SECRET_1_IN_HEX, EncodingUtils.encodeHexAsString(secret1));
        this.persistentStorage.put(IngConstants.Storage.SESSION_KEY_IN_HEX,
                EncodingUtils.encodeHexAsString(cryptoInitValues.getSessionKey()));
        this.persistentStorage.put(IngConstants.Storage.SESSION_KEY_AUTH_IN_HEX,
                EncodingUtils.encodeHexAsString(cryptoInitValues.getSessionKeyAuth()));
    }

    private byte[] executeGetAppCredentials(String regCode, byte[] sessionKey, byte[] sessionKeyAuth) {
        byte[] encryptedQueryData = IngCryptoUtils.generateEncryptedQueryData(regCode, sessionKey, sessionKeyAuth);
        String getAppCredentialsUrl = this.ingHelper.getUrl(IngConstants.RequestNames.GET_APP_CREDENTIALS);
        AppCredentialsResponse appCredentialsResponse =
                this.apiClient.getAppCredentials(getAppCredentialsUrl, encryptedQueryData);
        String encryptedAppCredentials = appCredentialsResponse.getxEnrolmentEncCredentials();

        return IngCryptoUtils.decryptAppCredentials(encryptedAppCredentials, sessionKey, sessionKeyAuth);
    }

    private int calcOtp(int otpCounter) {

        byte[] otpKey = EncodingUtils.decodeHexString(this.persistentStorage.get(IngConstants.Storage.OTP_KEY_HEX));
        int otp = IngCryptoUtils.calculateOtpForAuthentication(otpKey, otpCounter);
        otpCounter++;
        LOGGER.debug("Checking OTP value to save to Persistent Storage: " +  otpCounter);
        this.persistentStorage.put(IngConstants.Storage.OTP_COUNTER, otpCounter);

        return otp;
    }

    private void checkForUserInputError(HttpResponse httpResponse) throws LoginException {
        // We don't get a specific error msg if a user enters wrong ING ID, card ID, or response code,
        // just a 302 (that goes to an old page) when we try to initiate enrollment.
        if (httpResponse.getStatus() == HttpStatusCodes.STATUS_CODE_FOUND) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(
                    IngConstants.EndUserMessage.INCORRECT_LOGIN_CREDENTIALS.getKey());
        }
    }

    private void checkForChallengeExchangeError(BaseMobileResponseEntity responseEntity)
            throws LoginException {
        String returnCode = responseEntity.getReturnCode();

        if (IngConstants.ReturnCodes.OK.equalsIgnoreCase(returnCode)) {
            return;
        }

        if (IngConstants.ReturnCodes.NOK.equalsIgnoreCase(returnCode)) {
            Optional<String> errorText = responseEntity.getErrorText();
            if (errorText.isPresent()) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(new LocalizableKey(errorText.get()));
            } else {
                throw new IllegalStateException(IngConstants.LogMessage.CHALLENGE_EXCHANGE_ERROR);
            }
        }

        // Don't know if there are other codes than ok and nok, logging those here if so.
        LOGGER.warn(String.format("%s: %s", IngConstants.LogMessage.UNKNOWN_RETURN_CODE, returnCode));
        throw new IllegalStateException(IngConstants.LogMessage.CHALLENGE_EXCHANGE_ERROR);
    }
}
