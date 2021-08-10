package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankJavascriptStringFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.ChromeDriverConfig;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.WebDriverWrapper;

@RequiredArgsConstructor
public class DanskeBankPasswordAuthenticator implements PasswordAuthenticator {

    private final DanskeBankApiClient apiClient;
    private final String deviceId;
    private final DanskeBankConfiguration configuration;
    private final AgentTemporaryStorage agentTemporaryStorage;

    private String dynamicLogonJavascript;
    private String finalizePackage;

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {

        // Get the dynamic logon javascript
        HttpResponse getResponse =
                this.apiClient.collectDynamicLogonJavascript(
                        DanskeBankConstants.SecuritySystem.SERVICE_CODE_SC,
                        this.configuration.getBrand());
        // Add the authorization header from the response
        apiClient.saveAuthorizationHeader(getResponse);

        // Add method to return device information string
        this.dynamicLogonJavascript =
                DanskeBankConstants.Javascript.getDeviceInfo(
                                this.deviceId,
                                this.configuration.getMarketCode(),
                                this.configuration.getAppName(),
                                this.configuration.getAppVersion())
                        + getResponse.getBody(String.class);

        WebDriverWrapper driver =
                ChromeDriverInitializer.constructChromeDriver(
                        ChromeDriverConfig.builder()
                                .userAgent(DanskeBankConstants.Javascript.USER_AGENT)
                                .build(),
                        agentTemporaryStorage);
        try {
            // Execute javascript to get encrypted logon package and finalize package
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createLoginJavascript(
                            this.dynamicLogonJavascript, username, password));

            // Set the finalize package which will be used during finalization of the login
            this.finalizePackage =
                    driver.findElement(By.tagName("body")).getAttribute("logonPackage");

            // Finalize authentication
            finalizeAuthentication();
        } catch (HttpResponseException hre) {
            DanskeBankPasswordErrorHandler.throwError(hre);
        } finally {
            agentTemporaryStorage.remove(driver.getDriverId());
        }
    }

    private FinalizeAuthenticationResponse finalizeAuthentication() throws LoginException {
        // Get encrypted finalize package
        if (this.finalizePackage == null) {
            throw new IllegalStateException("Finalize Package was null, aborting login");
        }

        return this.apiClient.finalizeAuthentication(
                FinalizeAuthenticationRequest.createForServiceCode(this.finalizePackage));
    }
}
