package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.authenticator;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
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
import se.tink.integration.webdriver.WebDriverHelper;

@Slf4j
@RequiredArgsConstructor
public class DanskeBankNOAuthInitializer {

    private final DanskeBankNOApiClient apiClient;
    private final String deviceId;
    private final DanskeBankConfiguration configuration;
    private final WebDriverHelper webDriverHelper;

    String initializeSessionAndGetLogonPackage(
            String username, String serviceCode, WebDriver driver) throws AuthenticationException {

        // Fetch universal JS that handles logging in process
        String dynamicLoginJavascript = fetchDynamicLoginJavascriptAndSaveSessionHeader();

        // Prepare JS that will start login process for our user
        String userLoginJavascript =
                prepareDynamicLoginJavascriptForUser(dynamicLoginJavascript, username, serviceCode);

        // Execute javascript to get encrypted logon package
        ((JavascriptExecutor) driver).executeScript(userLoginJavascript);

        return waitForLogonPackage(driver)
                .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);
    }

    private String fetchDynamicLoginJavascriptAndSaveSessionHeader() {
        HttpResponse response =
                apiClient.collectDynamicLogonJavascript(
                        configuration.getSecuritySystem(), configuration.getBrand());

        // Add the authorization header from the response
        apiClient.saveAuthorizationHeader(response);

        return response.getBody(String.class);
    }

    private String prepareDynamicLoginJavascriptForUser(
            String dynamicLoginJavascript, String username, String serviceCode) {

        String dynamicLogonJavascriptForUser = getJsReturningDeviceInfo() + dynamicLoginJavascript;

        return DanskeBankJavascriptStringFormatter.createLoginJavascript(
                dynamicLogonJavascriptForUser, username, serviceCode);
    }

    String getJsReturningDeviceInfo() {
        return DanskeBankConstants.Javascript.getDeviceInfo(
                deviceId,
                configuration.getMarketCode(),
                configuration.getAppName(),
                configuration.getAppVersion());
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
}
