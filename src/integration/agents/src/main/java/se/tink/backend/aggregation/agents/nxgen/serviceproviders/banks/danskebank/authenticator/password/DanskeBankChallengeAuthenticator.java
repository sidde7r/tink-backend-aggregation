package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password;

import java.util.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.KeyCardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.ListOtpRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.ListOtpResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.MoreInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationResponse;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

public class DanskeBankChallengeAuthenticator extends DanskeBankAbstractAuthenticator implements KeyCardAuthenticator,
        AutoAuthenticator {

    private static final AggregationLogger log = new AggregationLogger(DanskeBankPasswordAuthenticator.class);
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private final DanskeBankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;
    private final String deviceId;
    private final DanskeBankConfiguration configuration;
    private String bindChallengeResponseBody;
    private String finalizePackage;
    private WebDriver driver;

    public DanskeBankChallengeAuthenticator(DanskeBankApiClient apiClient, PersistentStorage persistentStorage,
                                            Credentials credentials, String deviceId,
                                            DanskeBankConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.deviceId = deviceId;
        this.configuration = configuration;
    }

    @Override
    public KeyCardInitValues init(String username, String password) throws AuthenticationException, AuthorizationException {
        // Do normal service code login
        logonStepOne(username, password);

        // Bind device
        BindDeviceResponse bindDeviceResponse;
        try {
            bindDeviceResponse = this.apiClient.bindDevice(null,
                    BindDeviceRequest.create(DanskeBankConstants.Session.FRIENDLY_NAME));
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            if (response.getStatus() != 401) {
                throw new IllegalStateException();
            }

            bindDeviceResponse = response.getBody(BindDeviceResponse.class);
        }

        String moreInformation = StringEscapeUtils.unescapeJava(bindDeviceResponse.getMoreInformation());
        MoreInformationEntity moreInformationEntity = DanskeBankDeserializer
                .convertStringToObject(moreInformation, MoreInformationEntity.class);

        HttpResponse challengeResponse = this.apiClient.collectDynamicChallengeJavascript();

        // Create Javascript that will return device information
        String deviceInfoJavascript = DanskeBankConstants.Javascript.getDeviceInfo(this.deviceId, this.configuration.getMarketCode(),
                this.configuration.getAppName(), this.configuration.getAppVersion());
        this.bindChallengeResponseBody = deviceInfoJavascript + challengeResponse.getBody(String.class);

        try {
            this.driver = constructWebDriver();
            JavascriptExecutor js = (JavascriptExecutor) this.driver;
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createChallengeJavascript(
                            this.bindChallengeResponseBody, username, moreInformationEntity.getOtpChallenge()));

            KeyCardEntity keyCardEntity = DanskeBankDeserializer
                    .convertStringToObject(this.driver.findElement(By.tagName("body")).getAttribute("challengeInfo"),
                            KeyCardEntity.class);
            ListOtpResponse listOtpResponse = this.apiClient.listOtpInformation(ListOtpRequest.create(null));

            return new KeyCardInitValues(keyCardEntity.getSerialNumber(), keyCardEntity.getChallenge());
        } catch (Exception e) {
            if (this.driver != null) {
                this.driver.quit();
            }

            throw e;
        }
    }

    @Override
    public void authenticate(String code) throws AuthenticationException, AuthorizationException {
        try {
            JavascriptExecutor js = (JavascriptExecutor) this.driver;
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createChallengeAnswerJavascript(this.bindChallengeResponseBody, code));

            // Get step up token header
            String stepUpToken = this.driver.findElement(By.tagName("body")).getAttribute("bindStepUpToken");

            BindDeviceResponse bindDeviceResponse;
            try {
                bindDeviceResponse = this.apiClient.bindDevice(stepUpToken,
                        BindDeviceRequest.create(DanskeBankConstants.Session.FRIENDLY_NAME));
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
            String decryptedDeviceSecret = this.driver.findElement(By.tagName("body")).getAttribute("decryptedDeviceSecret");
            this.persistentStorage.put(DanskeBankConstants.Persist.DEVICE_SERIAL_NUMBER,
                    bindDeviceResponse.getDeviceSerialNumber());
            this.persistentStorage
                    .put(DanskeBankConstants.Persist.DEVICE_SECRET, decryptedDeviceSecret.replaceAll("\"", ""));
        } finally {
            if (this.driver != null) {
                this.driver.quit();
            }
        }
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        // Login
        try {
            logonStepOne(this.credentials.getField(Field.Key.USERNAME), this.credentials.getField(Field.Key.PASSWORD));
        } catch (AuthenticationException | AuthorizationException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        // Check device
        CheckDeviceResponse checkDeviceResponse;
        try {
            checkDeviceResponse = this.apiClient.checkDevice(
                    this.persistentStorage.get(DanskeBankConstants.Persist.DEVICE_SERIAL_NUMBER), null);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            if (response.getStatus() != 401) {
                log.warnExtraLong("Could not auto authenticate user: " + response.getBody(String.class),
                        DanskeBankConstants.LogTags.AUTHENTICATION_AUTO, e);
                throw e;
            }

            checkDeviceResponse = response.getBody(CheckDeviceResponse.class);
        }

        // Extract string of more information containing OtpChallenge
        String moreInformation = StringEscapeUtils.unescapeJava(checkDeviceResponse.getMoreInformation());
        MoreInformationEntity moreInformationEntity = DanskeBankDeserializer
                .convertStringToObject(moreInformation, MoreInformationEntity.class);

        // If another device has been pinned we can no longer sign in as trusted device.
        if (moreInformationEntity.isChallengeInvalid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        HttpResponse injectJsCheckStep = this.apiClient.collectDynamicChallengeJavascript();

        // Create Javascript that will return device information
        String deviceInfoJavascript = DanskeBankConstants.Javascript.getDeviceInfo(this.deviceId, this.configuration.getMarketCode(),
                this.configuration.getAppName(), this.configuration.getAppVersion());

        // Add device info Js to Danske Bank's inject Js
        String checkChallengeWithDeviceInfo = deviceInfoJavascript + injectJsCheckStep.getBody(String.class);

        // Execute Js to build step up token
        WebDriver driver = null;
        try {
            driver = constructWebDriver();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            // Initiate with username and OtpChallenge
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createInitStepUpTrustedDeviceJavascript(
                            checkChallengeWithDeviceInfo,
                            this.credentials.getField(Field.Key.USERNAME), moreInformationEntity.getOtpChallenge()));

            // Extract key card entity to get challenge for next Js execution
            KeyCardEntity keyCardEntity = DanskeBankDeserializer
                    .convertStringToObject(driver.findElement(By.tagName("body")).getAttribute("trustedChallengeInfo"),
                            KeyCardEntity.class);

            // Generate a JSON-object with device secret and challenge and encode it with base64
            String generateResponseInput = BASE64_ENCODER.encodeToString(
                    getGenerateResponseJson(keyCardEntity.getChallenge()).getBytes());

            // Inject the base64-string as input to generate response string
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createGenerateResponseJavascript(checkChallengeWithDeviceInfo,
                            this.credentials.getField(Field.Key.USERNAME), moreInformationEntity.getOtpChallenge(),
                            generateResponseInput));
            String responseData = driver.findElement(By.tagName("body")).getAttribute("trustedChallengeResponse");

            // Generate a JSON with response and device serial number and encode with base64
            String validateStepUpTrustedDeviceInput = BASE64_ENCODER.encodeToString(
                    getValidateStepUpTrustedDeviceJson(responseData.replaceAll("\"", "")).getBytes());

            // Execute a final Js to get the step up token
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createValidateStepUpTrustedDeviceJavascript(
                            checkChallengeWithDeviceInfo, this.credentials.getField(Field.Key.USERNAME),
                            moreInformationEntity.getOtpChallenge(), generateResponseInput,
                            validateStepUpTrustedDeviceInput));

            try {
                // Make final check to confirm with step up token
                checkDeviceResponse = this.apiClient.checkDevice(
                        this.persistentStorage.get(DanskeBankConstants.Persist.DEVICE_SERIAL_NUMBER),
                        driver.findElement(By.tagName("body")).getAttribute("trustedStepUpToken"));

                if (checkDeviceResponse.getError() != null) {
                    throw SessionError.SESSION_EXPIRED.exception();
                }
            } catch (HttpResponseException e) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } catch (Exception e) {
            if (driver != null) {
                log.infoExtraLong(driver.getPageSource(), LogTag.from("danskebank_autherror"));
            }

            throw e;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private void logonStepOne(String username, String password) throws AuthenticationException, AuthorizationException {
        // Get the dynamic logon javascript
        HttpResponse getResponse = this.apiClient.collectDynamicLogonJavascript(
                this.configuration.getSecuritySystem(), this.configuration.getBrand());

        // Add the authorization header from the response
        this.apiClient.addPersistentHeader("Authorization", getResponse.getHeaders().getFirst("Persistent-Auth"));

        // Create Javascript that will return device information
        String deviceInfoJavascript = DanskeBankConstants.Javascript.getDeviceInfo(this.deviceId, this.configuration.getMarketCode(),
                this.configuration.getAppName(), this.configuration.getAppVersion());

        // Add device information Javascript to dynamic logon Javascript
        String dynamicLogonJavascript = deviceInfoJavascript + getResponse.getBody(String.class);

        // Execute javascript to get encrypted logon package and finalize package
        WebDriver driver = null;
        try {
            driver = constructWebDriver();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createLoginJavascript(dynamicLogonJavascript, username, password));

            this.finalizePackage = driver.findElement(By.tagName("body")).getAttribute("finalizePackage");

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

    @Override
    protected FinalizeAuthenticationResponse finalizeAuthentication() {
        // Get encrypted finalize package
        if (this.finalizePackage == null) {
            throw new IllegalStateException("Finalize Package was null, aborting login");
        }

        return this.apiClient.finalizeAuthentication(
                FinalizeAuthenticationRequest.createForServiceCode(this.finalizePackage));

    }

    private String getGenerateResponseJson(String challenge) {
        JSONObject generateResponseJson = new JSONObject();
        try {
            generateResponseJson.put("devSecret", this.persistentStorage.get(DanskeBankConstants.Persist.DEVICE_SECRET));
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
            validateStepUpTrustedDeviceJson.put("devSerialNo", this.persistentStorage.get(
                    DanskeBankConstants.Persist.DEVICE_SERIAL_NUMBER));
        } catch (JSONException e) {
            throw new IllegalStateException();
        }

        return validateStepUpTrustedDeviceJson.toString();
    }

    private enum UserMessage implements LocalizableEnum {
        CREDENTIALS_VERIFICATION_ERROR(new LocalizableKey("Wrong challenge response input - Will retry login."));

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
