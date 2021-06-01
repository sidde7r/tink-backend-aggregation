package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.DanskeBankNOConstants.AUTHENTICATION_FINISH_URL;

import com.google.common.base.Strings;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.DanskeBankNOApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.DanskeBankNOConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankJavascriptStringFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.BindDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.BindDeviceResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticationResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeFirstWindow;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeInitializer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.integration.webdriver.WebDriverHelper;

@RequiredArgsConstructor
public class DanskeBankNOManualAuthenticator
        implements BankIdIframeInitializer, BankIdIframeAuthenticator {

    private final DanskeBankNOApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final WebDriverHelper webDriverHelper;
    private final Credentials credentials;

    private final DanskeBankNOAuthInitializer authInitializer;

    @Override
    public BankIdIframeFirstWindow initializeIframe(BankIdWebDriver webDriver) {
        String username = credentials.getField(Field.Key.USERNAME);
        String serviceCode = credentials.getField(Field.Key.PASSWORD);
        String bankIdPassword = credentials.getField(Field.Key.BANKID_PASSWORD);
        verifyCorrectCredentials(username, serviceCode, bankIdPassword);

        WebDriver driver = webDriver.getDriver();

        String logonPackage =
                authInitializer.initializeSessionAndGetLogonPackage(username, serviceCode, driver);
        authInitializer.sendLogonPackage(logonPackage);

        initBindDevice();
        openBankIdIFrame(driver);

        return BankIdIframeFirstWindow.ENTER_SSN;
    }

    @Override
    public String getSubstringOfUrlIndicatingAuthenticationFinish() {
        return AUTHENTICATION_FINISH_URL;
    }

    @Override
    public void handleBankIdAuthenticationResult(
            BankIdIframeAuthenticationResult authenticationResult) {
        // Page reload (destroy the iframe).
        // Read the `logonPackage` string which is used as `stepUpToken`.
        String stepUpToken =
                authInitializer
                        .waitForLogonPackage(authenticationResult.getWebDriver().getDriver())
                        .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);

        finalizeDeviceBinding(stepUpToken, authenticationResult.getWebDriver().getDriver());
    }

    private void verifyCorrectCredentials(String... credentials) {
        boolean credentialsInvalid = Stream.of(credentials).anyMatch(Strings::isNullOrEmpty);
        if (credentialsInvalid) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    private void initBindDevice() {
        // Bind device the first time in order to initialize a device binding using bankId.
        // We expect a 401 response.
        try {
            apiClient.bindDevice(
                    null, BindDeviceRequest.create(DanskeBankConstants.Session.FRIENDLY_NAME));
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            if (response.getStatus() != 401) {
                throw new IllegalStateException(e);
            }
        }
    }

    private void openBankIdIFrame(WebDriver driver) {
        HttpResponse dynamicJsResponse =
                apiClient.getNoBankIdDynamicJs(DanskeBankConstants.SecuritySystem.SERVICE_CODE_NS);
        String dynamicJs = dynamicJsResponse.getBody(String.class);

        String epochInSeconds = Long.toString(System.currentTimeMillis() / 1000);
        driver.get(DanskeBankNOConstants.NEMID_HTML_BOX_URL + epochInSeconds);

        JavascriptExecutor js = (JavascriptExecutor) driver;
        String formattedJs =
                DanskeBankJavascriptStringFormatter.createNOBankIdJavascript(dynamicJs);
        js.executeScript(formattedJs);
    }

    private String getStepupDynamicJs() {
        HttpResponse challengeResponse = apiClient.collectDynamicChallengeJavascript();

        // Create Javascript that will return device information
        String deviceInfoJavascript = authInitializer.getJsReturningDeviceInfo();

        return deviceInfoJavascript + challengeResponse.getBody(String.class);
    }

    private void finalizeDeviceBinding(String stepUpToken, WebDriver driver)
            throws AuthenticationException {
        BindDeviceResponse bindDeviceResponse;
        try {
            bindDeviceResponse =
                    apiClient.bindDevice(
                            stepUpToken,
                            BindDeviceRequest.create(DanskeBankConstants.Session.FRIENDLY_NAME));
        } catch (HttpResponseException hre) {
            HttpResponse response = hre.getResponse();
            if (response.getStatus() == 401) {
                throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(hre);
            }
            throw hre;
        }

        String stepupDynamicJs = getStepupDynamicJs();

        JavascriptExecutor js = (JavascriptExecutor) driver;

        js.executeScript(
                DanskeBankJavascriptStringFormatter.createCollectDeviceSecretJavascript(
                        stepupDynamicJs, bindDeviceResponse.getSharedSecret()));

        String decryptedDeviceSecret =
                webDriverHelper
                        .waitForElementWithAttribute(
                                driver, By.tagName("body"), "decryptedDeviceSecret")
                        .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);

        persistentStorage.put(
                DanskeBankConstants.Persist.DEVICE_SERIAL_NUMBER,
                bindDeviceResponse.getDeviceSerialNumber());
        persistentStorage.put(
                DanskeBankConstants.Persist.DEVICE_SECRET, decryptedDeviceSecret.replace("\"", ""));
    }
}
