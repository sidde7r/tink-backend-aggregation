package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
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
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankJavascriptStringFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.BindDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.BindDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.CheckDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.CodeAppChallengeAnswerEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.DanskeIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.DanskeIdInitRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.DanskeIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.DanskeIdStatusRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.DeviceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.InitOtpResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.KeyCardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.ListOtpRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.ListOtpResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.MoreInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationResponse;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
@RequiredArgsConstructor
public class DanskeBankChallengeAuthenticator
        implements TypedAuthenticator, AutoAuthenticator, KeyCardAuthenticator {
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final String STATIC_SALT = "iex5gei5aicoh$v*iXu1";

    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;
    private final DanskeBankApiClient apiClient;
    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;
    private final String deviceId;
    private final DanskeBankConfiguration configuration;

    private String bindChallengeResponseBody;
    private String finalizePackage;
    private WebDriver driver;
    private String keyCardOtpChallenge;
    private String userId;

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
        credentials.setSensitivePayload(Field.Key.USERNAME, username);
        credentials.setSensitivePayload(Field.Key.PASSWORD, password);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        logHashes(credentials);

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
                throw new IllegalStateException(e);
            }

            bindDeviceResponse = response.getBody(BindDeviceResponse.class);
        }

        setupBindChallengeResponseBody();

        DeviceEntity preferredDevice = getPreferredDevice();
        if (preferredDevice.isCodeApp()) {
            codeAppAuthentication(username, preferredDevice);
        } else if (preferredDevice.isOtpCard()
                || preferredDevice.isSecCard()
                || preferredDevice.isGemalto()) {
            this.keyCardOtpChallenge = getKeyCardOtpChallenge(bindDeviceResponse);
            KeyCardAuthenticationController keyCardAuthenticationController =
                    new KeyCardAuthenticationController(
                            catalog, supplementalInformationController, this);

            // Authenticate using key card (supplemental information)
            keyCardAuthenticationController.authenticate(credentials);
        } else if (preferredDevice.isDanskeId()) {
            danskeIdAuthentication(username, preferredDevice);
        } else {
            // Unknown device type.
            throw new IllegalStateException(
                    String.format("Unknown device type: %s.", preferredDevice.getDeviceType()));
        }
    }

    private void logHashes(Credentials credentials) {
        // There are a lot of invalid_credentials thrown.
        // Users often finally manages to provide correct credentials in 2nd or 3rd attempt.
        // We want to investigate if users have problems with providing username or password.
        // To achieve that - this logging will be helpful. We will check the hashes from
        // unsuccessful and successful authentications for the same credentialsId / userId and check
        // whether username hash or credentials hash changed.
        log.info(
                "[Danske DK] Hashes: {}, {}",
                BASE64_ENCODER
                        .encodeToString(
                                Hash.sha512(credentials.getField(Field.Key.USERNAME) + STATIC_SALT))
                        .substring(0, 6),
                BASE64_ENCODER
                        .encodeToString(
                                Hash.sha512(credentials.getField(Field.Key.PASSWORD) + STATIC_SALT))
                        .substring(0, 6));
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
        finalizeChallengeAuthentication(code, driver);
    }
    // --- KeyCardAuthenticator ---

    // +++ CodeAppAuthenticator +++
    private void codeAppAuthentication(String username, DeviceEntity preferredDevice)
            throws AuthenticationException, AuthorizationException {

        // Use NemId controller directly to avoid code replication
        DanskeBankNemIdCodeAppAuthenticator codeAppAuthenticator =
                new DanskeBankNemIdCodeAppAuthenticator(
                        apiClient, client, preferredDevice, username, bindChallengeResponseBody);
        NemIdCodeAppAuthenticationController nemIdAuthenticationController =
                new NemIdCodeAppAuthenticationController(
                        codeAppAuthenticator, supplementalInformationController, catalog);

        try {
            // Credentials are not needed for this implementation
            nemIdAuthenticationController.authenticate(null);
        } catch (ThirdPartyAppException e) {
            switch (e.getError()) {
                case CANCELLED:
                    throw ThirdPartyAppError.CANCELLED.exception(
                            UserMessage.CODE_APP_REJECTED_ERROR.getKey(), e);
                case TIMED_OUT:
                    throw ThirdPartyAppError.TIMED_OUT.exception(
                            UserMessage.CODE_APP_TIMEOUT_ERROR.getKey(), e);
                default:
                    throw new IllegalStateException(
                            String.format(
                                    "Unknown third party app exception error: %s.", e.getError()),
                            e);
            }
        }

        // Successful authentication, needs to finalize the JS execution
        NemIdCodeAppResponse response =
                (NemIdCodeAppResponse) nemIdAuthenticationController.getResponse();
        CodeAppChallengeAnswerEntity challengeAnswerEntity =
                CodeAppChallengeAnswerEntity.createFromPollResponse(response.getPollResponse());
        String challengeAnswer = SerializationUtils.serializeToString(challengeAnswerEntity);
        finalizeChallengeAuthentication(challengeAnswer, codeAppAuthenticator.getDriver());
    }
    // --- CodeAppAuthenticator ---

    private void finalizeChallengeAuthentication(String challengeAnswer, WebDriver driver)
            throws AuthenticationException {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createChallengeAnswerJavascript(
                            this.bindChallengeResponseBody,
                            StringEscapeUtils.escapeJava(challengeAnswer)));
            // Get step up token header
            String stepUpToken =
                    driver.findElement(By.tagName("body")).getAttribute("bindStepUpToken");
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
                            UserMessage.CREDENTIALS_VERIFICATION_ERROR.getKey(), hre);
                }
                throw hre;
            }
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createCollectDeviceSecretJavascript(
                            this.bindChallengeResponseBody, bindDeviceResponse.getSharedSecret()));
            // Persist decrypted device secret - necessary for login after device has been bounded
            String decryptedDeviceSecret =
                    driver.findElement(By.tagName("body")).getAttribute("decryptedDeviceSecret");
            this.persistentStorage.put(
                    DanskeBankConstants.Persist.DEVICE_SERIAL_NUMBER,
                    bindDeviceResponse.getDeviceSerialNumber());
            this.persistentStorage.put(
                    DanskeBankConstants.Persist.DEVICE_SECRET,
                    decryptedDeviceSecret.replaceAll("\"", ""));
        } finally {
            if (driver != null) {
                driver.quit();
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
            throw SessionError.SESSION_EXPIRED.exception(e);
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
                log.warn(
                        "tag={} Could not auto authenticate user, status {}",
                        DanskeBankConstants.LogTags.AUTHENTICATION_AUTO,
                        response.getStatus(),
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
            driver =
                    ChromeDriverInitializer.constructChromeDriver(
                            DanskeBankConstants.Javascript.USER_AGENT);
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
                log.info(
                        "tag={} Attribute 'trustedChallengeInfo' not found",
                        LogTag.from("danskebank_autherror"));
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
                throw SessionError.SESSION_EXPIRED.exception(e);
            }
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private FinalizeAuthenticationResponse finalizeAuthentication() throws LoginException {
        // Get encrypted finalize package
        if (this.finalizePackage == null) {
            throw new IllegalStateException("Finalize Package was null, aborting login");
        }

        FinalizeAuthenticationResponse response =
                this.apiClient.finalizeAuthentication(
                        FinalizeAuthenticationRequest.createForServiceCode(this.finalizePackage));

        this.userId = response.getUserId();
        persistentStorage.put(Storage.IDENTITY_INFO, response);
        return response;
    }

    private void logonStepOne(String username, String password)
            throws AuthenticationException, AuthorizationException {
        // Get the dynamic logon javascript
        HttpResponse getResponse =
                this.apiClient.collectDynamicLogonJavascript(
                        this.configuration.getSecuritySystem(), this.configuration.getBrand());

        // Add the authorization header from the response
        final String persistentAuth =
                getResponse
                        .getHeaders()
                        .getFirst(DanskeBankConstants.DanskeRequestHeaders.PERSISTENT_AUTH);
        // Store tokens in sensitive payload, so it will be masked from logs
        credentials.setSensitivePayload(
                DanskeBankConstants.DanskeRequestHeaders.AUTHORIZATION, persistentAuth);
        this.apiClient.addPersistentHeader(
                DanskeBankConstants.DanskeRequestHeaders.AUTHORIZATION, persistentAuth);

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
            driver =
                    ChromeDriverInitializer.constructChromeDriver(
                            DanskeBankConstants.Javascript.USER_AGENT);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createLoginJavascript(
                            dynamicLogonJavascript, username, password));

            this.finalizePackage =
                    driver.findElement(By.tagName("body")).getAttribute("logonPackage");

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
            this.driver =
                    ChromeDriverInitializer.constructChromeDriver(
                            DanskeBankConstants.Javascript.USER_AGENT);
            JavascriptExecutor js = (JavascriptExecutor) driver;
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
                .orElseThrow(LoginError.NO_AVAILABLE_SCA_METHODS::exception);
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
            throw new IllegalStateException(e);
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
            throw new IllegalStateException(e);
        }

        return validateStepUpTrustedDeviceJson.toString();
    }

    public enum UserMessage implements LocalizableEnum {
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

    public void danskeIdAuthentication(String username, DeviceEntity preferredDevice)
            throws AuthenticationException, AuthorizationException {
        InitOtpResponse initOtpResponse =
                this.apiClient.initOtp(
                        preferredDevice.getDeviceType(), preferredDevice.getDeviceSerialNumber());
        String otpChallenge = initOtpResponse.getOtpChallenge();
        DanskeIdInitResponse initResponse =
                apiClient.danskeIdInit(
                        new DanskeIdInitRequest(
                                DanskeBankConstants.DanskeIdFormValues.EXTERNALREF,
                                DanskeBankConstants.DanskeIdFormValues.EXTERNALTEXT,
                                username,
                                DanskeBankConstants.DanskeIdFormValues.EXTERNALUSERIDTYPE,
                                DanskeBankConstants.DanskeIdFormValues.MESSAGETEMPLATEID,
                                DanskeBankConstants.DanskeIdFormValues.OTPAPPTYPE,
                                DanskeBankConstants.DanskeIdFormValues.OTPREQUESTTYPE,
                                DanskeBankConstants.DanskeIdFormValues.PRODUCT));
        int otpRequestId = initResponse.getOtpRequestId();
        danskeIdPoll(username, otpRequestId);
        decryptOtpChallenge(username, otpChallenge, DanskeIdEntity.class);
        finalizeChallengeAuthentication(Integer.toString(otpRequestId), driver);
    }

    private void danskeIdPoll(String ExternalUserId, int OtpRequestId)
            throws ThirdPartyAppException {
        for (int i = 0; i < DanskeBankConstants.PollCodeTimeoutFilter.MAX_POLLS_COUNTER; i++) {
            Uninterruptibles.sleepUninterruptibly(5000, TimeUnit.MILLISECONDS);
            String danskeIdStatus = getDanskeIdStatus(ExternalUserId, OtpRequestId);
            switch (danskeIdStatus) {
                case DanskeBankConstants.DanskeIdStatusCodes.COMPLETED:
                    return;
                case DanskeBankConstants.DanskeIdStatusCodes.PENDING:
                    continue;
                case DanskeBankConstants.DanskeIdStatusCodes.EXPIRED:
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
                default:
                    break;
            }
        }
        throw ThirdPartyAppError.TIMED_OUT.exception();
    }

    private String getDanskeIdStatus(String ExternalUserId, int OtpRequestId) {
        return apiClient
                .getStatus(new DanskeIdStatusRequest(ExternalUserId, OtpRequestId))
                .getStatus();
    }
}
