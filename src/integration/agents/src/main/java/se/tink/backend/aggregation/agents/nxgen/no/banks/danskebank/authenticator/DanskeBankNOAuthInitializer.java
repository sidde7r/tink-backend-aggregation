package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.authenticator;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.DanskeBankNOApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankJavascriptStringFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.DanskeBankPasswordErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.WebDriverHelper;

@Slf4j
@RequiredArgsConstructor
public class DanskeBankNOAuthInitializer {

    private final DanskeBankNOApiClient apiClient;
    private final Credentials credentials;
    private final String deviceId;
    private final DanskeBankConfiguration configuration;
    private final WebDriverHelper webDriverHelper;

    String authenticateWithServiceCode(String username, String serviceCode)
            throws AuthenticationException {
        HttpResponse getResponse =
                apiClient.collectDynamicLogonJavascript(
                        configuration.getSecuritySystem(), configuration.getBrand());

        // Add the authorization header from the response
        final String persistentAuth =
                getResponse
                        .getHeaders()
                        .getFirst(DanskeBankConstants.DanskeRequestHeaders.PERSISTENT_AUTH);
        // Store tokens in sensitive payload, so it will be masked from logs
        credentials.setSensitivePayload(
                DanskeBankConstants.DanskeRequestHeaders.AUTHORIZATION, persistentAuth);
        apiClient.addPersistentHeader(
                DanskeBankConstants.DanskeRequestHeaders.AUTHORIZATION, persistentAuth);

        // Create Javascript that will return device information
        String deviceInfoJavascript = getDeviceInfoJavascript();

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
                            dynamicLogonJavascript, username, serviceCode));

            return waitForLogonPackage(driver)
                    .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    // Create Javascript that will return device information
    String getDeviceInfoJavascript() {
        return DanskeBankConstants.Javascript.getDeviceInfo(
                deviceId,
                configuration.getMarketCode(),
                configuration.getAppName(),
                configuration.getAppVersion());
    }

    void sendLogonPackage(String logonPackage)
            throws AuthenticationException, AuthorizationException {
        if (logonPackage == null) {
            throw new IllegalStateException("Finalize Package was null, aborting login");
        }

        try {
            apiClient.finalizeAuthentication(
                    FinalizeAuthenticationRequest.createForServiceCode(logonPackage));
        } catch (HttpResponseException hre) {
            DanskeBankPasswordErrorHandler.throwError(hre);
        }
    }

    Optional<String> waitForLogonPackage(WebDriver driver) {
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
}
