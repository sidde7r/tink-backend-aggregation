package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.authenticator;

import com.google.common.base.Strings;
import java.util.Base64;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.DanskeBankNOApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.DanskeBankNOConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankJavascriptStringFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.CheckDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.KeyCardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.MoreInformationEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.WebDriverHelper;

@Slf4j
@RequiredArgsConstructor
public class DanskeBankNOAutoAuthenticator implements AutoAuthenticator {

    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    private final DanskeBankNOApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;
    private final WebDriverHelper webDriverHelper;

    private final DanskeBankNOAuthInitializer authInitializer;

    @Override
    public void autoAuthenticate()
            throws SessionException, LoginException, BankServiceException, AuthorizationException {

        String username = credentials.getField(Field.Key.USERNAME);
        String serviceCode = credentials.getField(Field.Key.PASSWORD);
        String deviceSerialNumber =
                persistentStorage.get(DanskeBankConstants.Persist.DEVICE_SERIAL_NUMBER);

        verifyHasAllSessionValues(username, serviceCode, deviceSerialNumber);

        try {
            String logonPackage =
                    authInitializer.authenticateWithServiceCode(username, serviceCode);
            authInitializer.sendLogonPackage(logonPackage);
        } catch (AuthenticationException | AuthorizationException e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        }

        String otpChallenge = getPinnedDeviceOtpChallenge(deviceSerialNumber);
        authenticateWithOtpChallenge(username, deviceSerialNumber, otpChallenge);
    }

    private void verifyHasAllSessionValues(String... sessionValues) {
        boolean credentialsInvalid = Stream.of(sessionValues).anyMatch(Strings::isNullOrEmpty);
        if (credentialsInvalid) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private String getPinnedDeviceOtpChallenge(String deviceSerialNumber) throws SessionException {
        CheckDeviceResponse checkDeviceResponse;
        try {
            checkDeviceResponse = apiClient.checkDevice(deviceSerialNumber, null);
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
        String checkChallengeWithDeviceInfo = getStepupDynamicJs();

        // Execute Js to build step up token
        WebDriver driver = null;
        try {
            driver =
                    ChromeDriverInitializer.constructChromeDriver(
                            (DanskeBankConstants.Javascript.USER_AGENT));
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
                        apiClient.checkDevice(deviceSerialNumber, trustedStepUpToken);

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
                    persistentStorage.get(DanskeBankConstants.Persist.DEVICE_SECRET));
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
                    persistentStorage.get(DanskeBankConstants.Persist.DEVICE_SERIAL_NUMBER));
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }

        return validateStepUpTrustedDeviceJson.toString();
    }

    private String getStepupDynamicJs() {
        HttpResponse challengeResponse = apiClient.collectDynamicChallengeJavascript();

        // Create Javascript that will return device information
        String deviceInfoJavascript = authInitializer.getDeviceInfoJavascript();

        return deviceInfoJavascript + challengeResponse.getBody(String.class);
    }
}
