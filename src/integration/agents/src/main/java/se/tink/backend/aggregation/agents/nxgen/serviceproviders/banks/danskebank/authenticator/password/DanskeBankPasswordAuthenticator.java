package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankJavascriptStringFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.DanskeBankAbstractAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class DanskeBankPasswordAuthenticator extends DanskeBankAbstractAuthenticator
        implements PasswordAuthenticator {
    private static final AggregationLogger log =
            new AggregationLogger(DanskeBankPasswordAuthenticator.class);
    private final DanskeBankApiClient apiClient;
    private final String deviceId;
    private final DanskeBankConfiguration configuration;
    private String dynamicLogonJavascript;
    private String finalizePackage;

    public DanskeBankPasswordAuthenticator(
            DanskeBankApiClient apiClient, String deviceId, DanskeBankConfiguration configuration) {
        this.apiClient = apiClient;
        this.deviceId = deviceId;
        this.configuration = configuration;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        // Get the dynamic logon javascript
        HttpResponse getResponse =
                this.apiClient.collectDynamicLogonJavascript(
                        DanskeBankConstants.SecuritySystem.SERVICE_CODE_SC,
                        this.configuration.getBrand());

        // Add the authorization header from the response
        this.apiClient.addPersistentHeader(
                "Authorization", getResponse.getHeaders().getFirst("Persistent-Auth"));

        // Add method to return device information string
        this.dynamicLogonJavascript =
                DanskeBankConstants.Javascript.getDeviceInfo(
                                this.deviceId,
                                this.configuration.getMarketCode(),
                                this.configuration.getAppName(),
                                this.configuration.getAppVersion())
                        + getResponse.getBody(String.class);

        // Execute javascript to get encrypted logon package and finalize package
        WebDriver driver = null;
        try {
            driver = constructWebDriver();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createLoginJavascript(
                            this.dynamicLogonJavascript, username, password));

            // Set the finalize package which will be used during finalization of the login
            this.finalizePackage =
                    driver.findElement(By.tagName("body")).getAttribute("finalizePackage");

            // Finalize authentication
            finalizeAuthentication();
        } catch (HttpResponseException hre) {
            DanskeBankPasswordErrorHandler.throwError(hre);
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
}
