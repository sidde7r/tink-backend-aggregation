package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password;

import com.google.common.base.Strings;
import java.util.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankJavascriptStringFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.DanskeBankAbstractAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.BindDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.BindDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.CheckDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.CodeAppChallengeAnswerEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.CodeAppEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.DeviceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.InitOtpResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.KeyCardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.ListOtpRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.ListOtpResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.MoreInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.PollCodeAppResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationResponse;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DanskeBankChallengeAuthenticator extends DanskeBankAbstractAuthenticator
        implements MultiFactorAuthenticator, AutoAuthenticator, KeyCardAuthenticator {

    private static final AggregationLogger log =
            new AggregationLogger(DanskeBankChallengeAuthenticator.class);
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private final Catalog catalog;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final DanskeBankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;
    private final String deviceId;
    private final DanskeBankConfiguration configuration;
    private String bindChallengeResponseBody;
    private String finalizePackage;
    private WebDriver driver;
    private String keyCardOtpChallenge;
    private String userId;

    public DanskeBankChallengeAuthenticator(
            Catalog catalog,
            SupplementalInformationHelper supplementalInformationHelper,
            DanskeBankApiClient apiClient,
            PersistentStorage persistentStorage,
            Credentials credentials,
            String deviceId,
            DanskeBankConfiguration configuration) {
        this.catalog = catalog;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.deviceId = deviceId;
        this.configuration = configuration;
    }

    @Override
    public CredentialsTypes getType() {
        // TODO: Change to a multifactor type when supported
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        // Determine if we should do KeyCard Authentication or CodeApp Authentication.
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        // Do normal service code login
        logonStepOne(username, password);

        // Bind device
        BindDeviceResponse bindDeviceResponse;
        try {
            bindDeviceResponse =
                    this.apiClient.bindDevice(
                            null,
                            BindDeviceRequest.create(DanskeBankConstants.Session.FRIENDLY_NAME));
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            if (response.getStatus() != 401) {
                throw new IllegalStateException();
            }

            bindDeviceResponse = response.getBody(BindDeviceResponse.class);
        }

        setupBindChallengeResponseBody();

        DeviceEntity preferredDevice = getPreferredDevice();
        if (preferredDevice.isCodeApp()) {
            codeAppAuthentication(username, preferredDevice);
        } else if (preferredDevice.isOtpCard() || preferredDevice.isSecCard()) {
            this.keyCardOtpChallenge = getKeyCardOtpChallenge(bindDeviceResponse);
            KeyCardAuthenticationController keyCardAuthenticationController =
                    new KeyCardAuthenticationController(
                            catalog, supplementalInformationHelper, this);

            // Authenticate using key card (supplemental information)
            keyCardAuthenticationController.authenticate(credentials);
        } else {
            // Unknown device type.
            throw new IllegalStateException(
                    String.format("Unknown device type: %s.", preferredDevice.getDeviceType()));
        }
    }

    // +++ KeyCardAuthenticator +++
    @Override
    public KeyCardInitValues init(String username, String password)
            throws AuthenticationException, AuthorizationException {

        // For Codecard authentication in DK, they have switched to use the userId instead of
        // username
        KeyCardEntity keyCardEntity =
                decryptOtpChallenge(this.userId, this.keyCardOtpChallenge, KeyCardEntity.class);
        return new KeyCardInitValues(keyCardEntity.getSerialNumber(), keyCardEntity.getChallenge());
    }

    @Override
    public void authenticate(String code) throws AuthenticationException, AuthorizationException {
        finalizeChallengeAuthentication(code);
    }
    // --- KeyCardAuthenticator ---

    // +++ CodeAppAuthenticator +++
    private void codeAppAuthentication(String username, DeviceEntity preferredDevice)
            throws AuthenticationException, AuthorizationException {
        // Initiate the code app auth by requesting a new otp challenge.
        // The otp challenge we will get is an encrypted message containing the code app ticket and
        // url to poll the auth status.
        // The user will get a push notification to their device as a result of this request.
        InitOtpResponse initOtpResponse =
                this.apiClient.initOtp(
                        preferredDevice.getDeviceType(), preferredDevice.getDeviceSerialNumber());

        String otpChallenge = initOtpResponse.getOtpChallenge();
        CodeAppEntity codeAppEntity =
                decryptOtpChallenge(username, otpChallenge, CodeAppEntity.class);

        // This request is a long polling request, i.e. it will respond when either the
        // authentication timed out
        // or the user signed/cancelled in the code app.
        PollCodeAppResponse pollResponse =
                this.apiClient.pollCodeApp(codeAppEntity.getPollURL(), codeAppEntity.getToken());

        String pollStatus = pollResponse.getStatus();
        switch (pollStatus.toLowerCase()) {
            case DanskeBankConstants.CodeApp.STATUS_OK:
                break;
            case DanskeBankConstants.CodeApp.STATUS_TIMEOUT:
                throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                        UserMessage.CODE_APP_TIMEOUT_ERROR.getKey());
            default:
                throw new IllegalStateException(
                        String.format("Unknown code app poll response: %s.", pollStatus));
        }

        if (!pollResponse.isConfirmation()) {
            // This means the user rejected the signature request in the code app.
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                    UserMessage.CODE_APP_REJECTED_ERROR.getKey());
        }

        CodeAppChallengeAnswerEntity challengeAnswerEntity =
                CodeAppChallengeAnswerEntity.createFromPollResponse(pollResponse);

        String challengeAnswer = SerializationUtils.serializeToString(challengeAnswerEntity);
        finalizeChallengeAuthentication(challengeAnswer);
    }
    // --- CodeAppAuthenticator ---

    private void finalizeChallengeAuthentication(String challengeAnswer)
            throws AuthenticationException, AuthorizationException {
        try {
            JavascriptExecutor js = (JavascriptExecutor) this.driver;

            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createChallengeAnswerJavascript(
                            this.bindChallengeResponseBody,
                            StringEscapeUtils.escapeJava(challengeAnswer)));

            // Get step up token header
            String stepUpToken =
                    this.driver.findElement(By.tagName("body")).getAttribute("bindStepUpToken");

            BindDeviceResponse bindDeviceResponse;
            try {
                bindDeviceResponse =
                        this.apiClient.bindDevice(
                                stepUpToken,
                                BindDeviceRequest.create(
                                        DanskeBankConstants.Session.FRIENDLY_NAME));
            } catch (HttpResponseException hre) {
                HttpResponse response = hre.getResponse();
                if (response.getStatus() == 401) {
                    throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                            UserMessage.CREDENTIALS_VERIFICATION_ERROR.getKey());
                }

                throw hre;
            }

            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createCollectDeviceSecretJavascript(
                            this.bindChallengeResponseBody, bindDeviceResponse.getSharedSecret()));

            // Persist decrypted device secret - necessary for login after device has been bounded
            String decryptedDeviceSecret =
                    this.driver
                            .findElement(By.tagName("body"))
                            .getAttribute("decryptedDeviceSecret");
            this.persistentStorage.put(
                    DanskeBankConstants.Persist.DEVICE_SERIAL_NUMBER,
                    bindDeviceResponse.getDeviceSerialNumber());
            this.persistentStorage.put(
                    DanskeBankConstants.Persist.DEVICE_SECRET,
                    decryptedDeviceSecret.replaceAll("\"", ""));
        } finally {
            if (this.driver != null) {
                this.driver.quit();
            }
        }
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {
        // Login
        try {
            logonStepOne(
                    this.credentials.getField(Field.Key.USERNAME),
                    this.credentials.getField(Field.Key.PASSWORD));
        } catch (AuthenticationException | AuthorizationException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        // Check device
        CheckDeviceResponse checkDeviceResponse;
        try {
            checkDeviceResponse =
                    this.apiClient.checkDevice(
                            this.persistentStorage.get(
                                    DanskeBankConstants.Persist.DEVICE_SERIAL_NUMBER),
                            null);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            if (response.getStatus() != 401) {
                log.warnExtraLong(
                        "Could not auto authenticate user: " + response.getBody(String.class),
                        DanskeBankConstants.LogTags.AUTHENTICATION_AUTO,
                        e);
                throw e;
            }

            checkDeviceResponse = response.getBody(CheckDeviceResponse.class);
        }

        // Extract string of more information containing OtpChallenge
        String moreInformation =
                StringEscapeUtils.unescapeJava(checkDeviceResponse.getMoreInformation());
        MoreInformationEntity moreInformationEntity =
                DanskeBankDeserializer.convertStringToObject(
                        moreInformation, MoreInformationEntity.class);

        // If another device has been pinned we can no longer sign in as trusted device.
        if (moreInformationEntity.isChallengeInvalid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        HttpResponse injectJsCheckStep = this.apiClient.collectDynamicChallengeJavascript();

        // Create Javascript that will return device information
        String deviceInfoJavascript =
                DanskeBankConstants.Javascript.getDeviceInfo(
                        this.deviceId,
                        this.configuration.getMarketCode(),
                        this.configuration.getAppName(),
                        this.configuration.getAppVersion());

        // Add device info Js to Danske Bank's inject Js
        String checkChallengeWithDeviceInfo =
                deviceInfoJavascript + injectJsCheckStep.getBody(String.class);

        // Execute Js to build step up token
        WebDriver driver = null;
        try {
            driver = constructWebDriver();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            // Initiate with username and OtpChallenge
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createInitStepUpTrustedDeviceJavascript(
                            checkChallengeWithDeviceInfo,
                            this.credentials.getField(Field.Key.USERNAME),
                            moreInformationEntity.getOtpChallenge()));

            // Extract key card entity to get challenge for next Js execution
            String challengeInfo =
                    driver.findElement(By.tagName("body")).getAttribute("trustedChallengeInfo");
            // if no challengeInfo available, force a new device pinning
            if (Strings.isNullOrEmpty(challengeInfo)) {
                log.infoExtraLong(driver.getPageSource(), LogTag.from("danskebank_autherror"));
                throw SessionError.SESSION_EXPIRED.exception();
            }
            KeyCardEntity keyCardEntity =
                    DanskeBankDeserializer.convertStringToObject(
                            challengeInfo, KeyCardEntity.class);

            // Generate a JSON-object with device secret and challenge and encode it with base64
            String generateResponseInput =
                    BASE64_ENCODER.encodeToString(
                            getGenerateResponseJson(keyCardEntity.getChallenge()).getBytes());

            // Inject the base64-string as input to generate response string
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createGenerateResponseJavascript(
                            checkChallengeWithDeviceInfo,
                            this.credentials.getField(Field.Key.USERNAME),
                            moreInformationEntity.getOtpChallenge(),
                            generateResponseInput));
            String responseData =
                    driver.findElement(By.tagName("body")).getAttribute("trustedChallengeResponse");

            // Generate a JSON with response and device serial number and encode with base64
            String validateStepUpTrustedDeviceInput =
                    BASE64_ENCODER.encodeToString(
                            getValidateStepUpTrustedDeviceJson(responseData.replaceAll("\"", ""))
                                    .getBytes());

            // Execute a final Js to get the step up token
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createValidateStepUpTrustedDeviceJavascript(
                            checkChallengeWithDeviceInfo,
                            this.credentials.getField(Field.Key.USERNAME),
                            moreInformationEntity.getOtpChallenge(),
                            generateResponseInput,
                            validateStepUpTrustedDeviceInput));

            try {
                // Make final check to confirm with step up token
                checkDeviceResponse =
                        this.apiClient.checkDevice(
                                this.persistentStorage.get(
                                        DanskeBankConstants.Persist.DEVICE_SERIAL_NUMBER),
                                driver.findElement(By.tagName("body"))
                                        .getAttribute("trustedStepUpToken"));

                if (checkDeviceResponse.getError() != null) {
                    throw SessionError.SESSION_EXPIRED.exception();
                }
            } catch (HttpResponseException e) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    @Override
    protected FinalizeAuthenticationResponse finalizeAuthentication() {
        // Get encrypted finalize package
        if (this.finalizePackage == null) {
            throw new IllegalStateException("Finalize Package was null, aborting login");
        }

        FinalizeAuthenticationResponse response =
                this.apiClient.finalizeAuthentication(
                        FinalizeAuthenticationRequest.createForServiceCode(this.finalizePackage));

        this.userId = response.getUserId();
        return response;
    }

    private void logonStepOne(String username, String password)
            throws AuthenticationException, AuthorizationException {
        // Get the dynamic logon javascript
        HttpResponse getResponse =
                this.apiClient.collectDynamicLogonJavascript(
                        this.configuration.getSecuritySystem(), this.configuration.getBrand());

        // Add the authorization header from the response
        this.apiClient.addPersistentHeader(
                "Authorization", getResponse.getHeaders().getFirst("Persistent-Auth"));

        // Create Javascript that will return device information
        String deviceInfoJavascript =
                DanskeBankConstants.Javascript.getDeviceInfo(
                        this.deviceId,
                        this.configuration.getMarketCode(),
                        this.configuration.getAppName(),
                        this.configuration.getAppVersion());

        // Add device information Javascript to dynamic logon Javascript
        String dynamicLogonJavascript = deviceInfoJavascript + getResponse.getBody(String.class);

        // Execute javascript to get encrypted logon package and finalize package
        WebDriver driver = null;
        try {
            driver = constructWebDriver();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createLoginJavascript(
                            dynamicLogonJavascript, username, password));

            this.finalizePackage =
                    driver.findElement(By.tagName("body")).getAttribute("finalizePackage");

            // Finalize authentication
            try {
                finalizeAuthentication();
            } catch (HttpResponseException hre) {
                DanskeBankPasswordErrorHandler.throwError(hre);
            }
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private <T> T decryptOtpChallenge(String username, String otpChallenge, Class<T> clazz) {
        try {
            this.driver = constructWebDriver();
            JavascriptExecutor js = (JavascriptExecutor) this.driver;
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createChallengeJavascript(
                            this.bindChallengeResponseBody, username, otpChallenge));

            // The JavaScript populate the DOM element body->challengeInfo with the decrypted
            // result.
            String decryptedChallenge =
                    this.driver.findElement(By.tagName("body")).getAttribute("challengeInfo");
            return DanskeBankDeserializer.convertStringToObject(decryptedChallenge, clazz);
        } catch (Exception e) {
            if (this.driver != null) {
                this.driver.quit();
            }
            throw e;
        }
    }

    private String getKeyCardOtpChallenge(BindDeviceResponse bindDeviceResponse) {
        String moreInformation =
                StringEscapeUtils.unescapeJava(bindDeviceResponse.getMoreInformation());
        MoreInformationEntity moreInformationEntity =
                DanskeBankDeserializer.convertStringToObject(
                        moreInformation, MoreInformationEntity.class);
        return moreInformationEntity.getOtpChallenge();
    }

    private DeviceEntity getPreferredDevice() {
        ListOtpResponse listOtpResponse =
                this.apiClient.listOtpInformation(ListOtpRequest.create(null));
        return listOtpResponse
                .getPreferredDevice()
                .orElseThrow(
                        () -> new IllegalStateException("User does not have a preferred device."));
    }

    private void setupBindChallengeResponseBody() {
        // This is done once but the result is used throughout the authentication process.
        HttpResponse challengeResponse = this.apiClient.collectDynamicChallengeJavascript();

        // Create Javascript that will return device information
        String deviceInfoJavascript =
                DanskeBankConstants.Javascript.getDeviceInfo(
                        this.deviceId,
                        this.configuration.getMarketCode(),
                        this.configuration.getAppName(),
                        this.configuration.getAppVersion());
        this.bindChallengeResponseBody =
                deviceInfoJavascript + challengeResponse.getBody(String.class);
    }

    private String getGenerateResponseJson(String challenge) {
        JSONObject generateResponseJson = new JSONObject();
        try {
            generateResponseJson.put(
                    "devSecret",
                    this.persistentStorage.get(DanskeBankConstants.Persist.DEVICE_SECRET));
            generateResponseJson.put("challenge", challenge);
        } catch (JSONException e) {
            throw new IllegalStateException();
        }

        return generateResponseJson.toString();
    }

    private String getValidateStepUpTrustedDeviceJson(String responseData) {
        JSONObject validateStepUpTrustedDeviceJson = new JSONObject();
        try {
            validateStepUpTrustedDeviceJson.put("responseData", responseData);
            validateStepUpTrustedDeviceJson.put(
                    "devSerialNo",
                    this.persistentStorage.get(DanskeBankConstants.Persist.DEVICE_SERIAL_NUMBER));
        } catch (JSONException e) {
            throw new IllegalStateException();
        }

        return validateStepUpTrustedDeviceJson.toString();
    }

    private enum UserMessage implements LocalizableEnum {
        CREDENTIALS_VERIFICATION_ERROR(
                new LocalizableKey("Wrong challenge response input - Will retry login.")),
        CODE_APP_TIMEOUT_ERROR(new LocalizableKey("Code app authentication timed out.")),
        CODE_APP_REJECTED_ERROR(
                new LocalizableKey("Code app authentication was rejected by user."));

        private final LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return this.userMessage;
        }
    }
}
