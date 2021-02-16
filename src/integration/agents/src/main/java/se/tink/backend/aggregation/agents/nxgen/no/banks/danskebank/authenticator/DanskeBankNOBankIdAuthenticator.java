package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.authenticator;

import com.google.common.base.Strings;
import java.util.Base64;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.DanskeBankNOApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.DanskeBankNOConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankJavascriptStringFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.DanskeBankPasswordErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.BindDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.BindDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.CheckDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.KeyCardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.MoreInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidiframe.BankIdIframeSSAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidiframe.initializer.BankIdIframeInitializer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidiframe.initializer.IframeInitializer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.integration.webdriver.WebDriverHelper;
import se.tink.integration.webdriver.WebDriverInitializer;
import se.tink.libraries.i18n.Catalog;

@RequiredArgsConstructor
public class DanskeBankNOBankIdAuthenticator implements TypedAuthenticator, AutoAuthenticator {
    private static final Logger log =
            LoggerFactory.getLogger(DanskeBankNOBankIdAuthenticator.class);
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    private final DanskeBankNOApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;
    private final String deviceId;
    private final DanskeBankConfiguration configuration;
    private final WebDriverHelper webDriverHelper;
    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;

    @Override
    public CredentialsTypes getType() {
        // TODO: Change to a multifactor type when supported
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, LoginException, BankServiceException, AuthorizationException {

        String username = credentials.getField(Field.Key.USERNAME);
        String serviceCode = credentials.getField(Field.Key.PASSWORD);
        String deviceSerialNumber =
                this.persistentStorage.get(DanskeBankConstants.Persist.DEVICE_SERIAL_NUMBER);

        if (Strings.isNullOrEmpty(username)
                || Strings.isNullOrEmpty(serviceCode)
                || Strings.isNullOrEmpty(deviceSerialNumber)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        try {
            String logonPackage = authenticateWithServiceCode(username, serviceCode);
            sendLogonPackage(logonPackage);
        } catch (AuthenticationException | AuthorizationException e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        }

        String otpChallenge = getPinnedDeviceOtpChallenge(deviceSerialNumber);
        authenticateWithOtpChallenge(username, deviceSerialNumber, otpChallenge);
    }

    private String getPinnedDeviceOtpChallenge(String deviceSerialNumber) throws SessionException {
        CheckDeviceResponse checkDeviceResponse;
        try {
            checkDeviceResponse = this.apiClient.checkDevice(deviceSerialNumber, null);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            if (response.getStatus() != 401) {
                log.warn("Could not auto authenticate user, status " + response.getStatus(), e);
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

        return moreInformationEntity.getOtpChallenge();
    }

    private void authenticateWithOtpChallenge(
            String username, String deviceSerialNumber, String otpChallenge)
            throws SessionException {
        String checkChallengeWithDeviceInfo = this.getStepupDynamicJs();

        // Execute Js to build step up token
        WebDriver driver = null;
        try {
            driver =
                    WebDriverInitializer.constructWebDriver(
                            DanskeBankConstants.Javascript.USER_AGENT);
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Initiate with username and OtpChallenge
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createInitStepUpTrustedDeviceJavascript(
                            checkChallengeWithDeviceInfo, username, otpChallenge));

            // Extract key card entity to get challenge for next Js execution
            String challengeInfo =
                    webDriverHelper
                            .waitForElementWithAttribute(
                                    driver, By.tagName("body"), "trustedChallengeInfo")
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception);

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
                            username,
                            otpChallenge,
                            generateResponseInput));

            String responseData =
                    webDriverHelper
                            .waitForElementWithAttribute(
                                    driver, By.tagName("body"), "trustedChallengeResponse")
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception);

            // Generate a JSON with response and device serial number and encode with base64
            String validateStepUpTrustedDeviceInput =
                    BASE64_ENCODER.encodeToString(
                            getValidateStepUpTrustedDeviceJson(responseData.replace("\"", ""))
                                    .getBytes());

            // Execute a final Js to get the step up token
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createValidateStepUpTrustedDeviceJavascript(
                            checkChallengeWithDeviceInfo,
                            username,
                            otpChallenge,
                            generateResponseInput,
                            validateStepUpTrustedDeviceInput));

            String trustedStepUpToken =
                    webDriverHelper
                            .waitForElementWithAttribute(
                                    driver, By.tagName("body"), "trustedStepUpToken")
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception);

            try {
                // Make final check to confirm with step up token
                CheckDeviceResponse checkDeviceResponse =
                        this.apiClient.checkDevice(deviceSerialNumber, trustedStepUpToken);

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

    private String getGenerateResponseJson(String challenge) {
        JSONObject generateResponseJson = new JSONObject();
        try {
            generateResponseJson.put(
                    DanskeBankNOConstants.JsonKeys.DEV_SECRET,
                    this.persistentStorage.get(DanskeBankConstants.Persist.DEVICE_SECRET));
            generateResponseJson.put(DanskeBankNOConstants.JsonKeys.CHALLENGE, challenge);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }

        return generateResponseJson.toString();
    }

    private String getValidateStepUpTrustedDeviceJson(String responseData) {
        JSONObject validateStepUpTrustedDeviceJson = new JSONObject();
        try {
            validateStepUpTrustedDeviceJson.put(
                    DanskeBankNOConstants.JsonKeys.RESPONSE_DATA, responseData);
            validateStepUpTrustedDeviceJson.put(
                    DanskeBankNOConstants.JsonKeys.DEV_SERIAL_NUMBER,
                    this.persistentStorage.get(DanskeBankConstants.Persist.DEVICE_SERIAL_NUMBER));
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }

        return validateStepUpTrustedDeviceJson.toString();
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String username = credentials.getField(Field.Key.USERNAME);
        String serviceCode = credentials.getField(Field.Key.PASSWORD);
        String bankIdPassword = credentials.getField(Field.Key.BANKID_PASSWORD);
        if (Strings.isNullOrEmpty(username)
                || Strings.isNullOrEmpty(serviceCode)
                || Strings.isNullOrEmpty(bankIdPassword)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        String logonPackage = authenticateWithServiceCode(username, serviceCode);
        sendLogonPackage(logonPackage);

        initBindDevice();
        String stepUpToken = authenticateWithBankId(username, bankIdPassword);
        finalizeDeviceBinding(stepUpToken);
    }

    private void initBindDevice() {
        // Bind device the first time in order to initialize a device binding using bankId.
        // We expect a 401 response.
        try {
            this.apiClient.bindDevice(
                    null, BindDeviceRequest.create(DanskeBankConstants.Session.FRIENDLY_NAME));
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            if (response.getStatus() != 401) {
                throw new IllegalStateException(e);
            }
        }
    }

    private String getStepupDynamicJs() {
        HttpResponse challengeResponse = this.apiClient.collectDynamicChallengeJavascript();

        // Create Javascript that will return device information
        String deviceInfoJavascript =
                DanskeBankConstants.Javascript.getDeviceInfo(
                        this.deviceId,
                        this.configuration.getMarketCode(),
                        this.configuration.getAppName(),
                        this.configuration.getAppVersion());
        return deviceInfoJavascript + challengeResponse.getBody(String.class);
    }

    private void finalizeDeviceBinding(String stepUpToken) throws AuthenticationException {
        BindDeviceResponse bindDeviceResponse;
        try {
            bindDeviceResponse =
                    this.apiClient.bindDevice(
                            stepUpToken,
                            BindDeviceRequest.create(DanskeBankConstants.Session.FRIENDLY_NAME));
        } catch (HttpResponseException hre) {
            HttpResponse response = hre.getResponse();
            if (response.getStatus() == 401) {
                throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(hre);
            }
            throw hre;
        }

        String stepupDynamicJs = this.getStepupDynamicJs();

        WebDriver driver = null;
        try {
            driver =
                    WebDriverInitializer.constructWebDriver(
                            DanskeBankConstants.Javascript.USER_AGENT);
            JavascriptExecutor js = (JavascriptExecutor) driver;

            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createCollectDeviceSecretJavascript(
                            stepupDynamicJs, bindDeviceResponse.getSharedSecret()));

            String decryptedDeviceSecret =
                    webDriverHelper
                            .waitForElementWithAttribute(
                                    driver, By.tagName("body"), "decryptedDeviceSecret")
                            .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);

            this.persistentStorage.put(
                    DanskeBankConstants.Persist.DEVICE_SERIAL_NUMBER,
                    bindDeviceResponse.getDeviceSerialNumber());
            this.persistentStorage.put(
                    DanskeBankConstants.Persist.DEVICE_SECRET,
                    decryptedDeviceSecret.replace("\"", ""));
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private Optional<String> waitForLogonPackage(WebDriver driver) {
        // Look for the logonPackage in the main dom.
        // `logonPackage` is assigned in the dom by the JavaScript snippet we constructed.
        waitAfterSubmitingForm(driver);
        driver.switchTo().defaultContent();
        Optional<String> logonPackage =
                webDriverHelper.waitForElementWithAttribute(
                        driver, By.tagName("body"), "logonPackage");
        if (!logonPackage.isPresent()) {
            log.warn("No logon package, please verify source: {}", driver.getPageSource());
        }
        return logonPackage;
    }

    private void waitAfterSubmitingForm(WebDriver driver) {
        webDriverHelper.sleep(2000);
        log.info("Before switching to default content: {}", driver.getPageSource());
    }

    private String authenticateWithBankId(String username, String bankIdPassword)
            throws AuthenticationException {
        HttpResponse dynamicJsResponse =
                this.apiClient.getNoBankIdDynamicJs(
                        DanskeBankConstants.SecuritySystem.SERVICE_CODE_NS);
        String dynamicJs = dynamicJsResponse.getBody(String.class);

        WebDriver driver = null;
        try {
            driver =
                    WebDriverInitializer.constructWebDriver(
                            DanskeBankConstants.Javascript.USER_AGENT);

            String epochInSeconds = Long.toString(System.currentTimeMillis() / 1000);
            driver.get(DanskeBankNOConstants.NEMID_HTML_BOX_URL + epochInSeconds);

            JavascriptExecutor js = (JavascriptExecutor) driver;
            String formattedJs =
                    DanskeBankJavascriptStringFormatter.createNOBankIdJavascript(dynamicJs);
            js.executeScript(formattedJs);

            IframeInitializer iframeInitializer =
                    new BankIdIframeInitializer(username, driver, webDriverHelper);

            BankIdIframeSSAuthenticationController bankIdIframeSSAuthenticationController =
                    new BankIdIframeSSAuthenticationController(
                            webDriverHelper,
                            driver,
                            iframeInitializer,
                            supplementalInformationController,
                            catalog);
            bankIdIframeSSAuthenticationController.doLogin(bankIdPassword);

            // Page reload (destroy the iframe).
            // Read the `logonPackage` string which is used as `stepUpToken`.
            return waitForLogonPackage(driver)
                    .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private String authenticateWithServiceCode(String username, String serviceCode)
            throws AuthenticationException {
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
                    WebDriverInitializer.constructWebDriver(
                            DanskeBankConstants.Javascript.USER_AGENT);
            JavascriptExecutor js = (JavascriptExecutor) driver;

            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createLoginJavascript(
                            dynamicLogonJavascript, username, serviceCode));

            return waitForLogonPackage(driver)
                    .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private void sendLogonPackage(String logonPackage)
            throws AuthenticationException, AuthorizationException {
        if (logonPackage == null) {
            throw new IllegalStateException("Finalize Package was null, aborting login");
        }

        try {
            this.apiClient.finalizeAuthentication(
                    FinalizeAuthenticationRequest.createForServiceCode(logonPackage));
        } catch (HttpResponseException hre) {
            DanskeBankPasswordErrorHandler.throwError(hre);
        }
    }
}
