package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankJavascriptStringFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.CodeAppEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.DeviceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.InitOtpResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppAuthenticator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.ChromeDriverConfig;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.WebDriverWrapper;

public class DanskeBankNemIdCodeAppAuthenticator extends NemIdCodeAppAuthenticator<CodeAppEntity> {

    private final DanskeBankApiClient apiClient;
    private final String username;
    private final DeviceEntity preferredDevice;
    private final String bindChallengeResponseBody;
    private final AgentTemporaryStorage agentTemporaryStorage;

    private WebDriverWrapper driver;

    public DanskeBankNemIdCodeAppAuthenticator(
            DanskeBankApiClient apiClient,
            TinkHttpClient client,
            DeviceEntity preferredDevice,
            String username,
            String bindChallengeResponseBody,
            AgentTemporaryStorage agentTemporaryStorage) {
        super(client);
        this.apiClient = apiClient;
        this.username = username;
        this.preferredDevice = preferredDevice;
        this.bindChallengeResponseBody = bindChallengeResponseBody;
        this.agentTemporaryStorage = agentTemporaryStorage;
    }

    @Override
    protected CodeAppEntity initiateAuthentication() {
        // Initiate the code app auth by requesting a new otp challenge.
        // The otp challenge we will get is an encrypted message containing the code app ticket and
        // url to poll the auth status.
        // The user will get a push notification to their device as a result of this request.
        InitOtpResponse initOtpResponse =
                this.apiClient.initOtp(
                        preferredDevice.getDeviceType(), preferredDevice.getDeviceSerialNumber());

        String otpChallenge = initOtpResponse.getOtpChallenge();
        return decryptOtpChallenge(username, otpChallenge, CodeAppEntity.class);
    }

    @Override
    protected String getPollUrl(CodeAppEntity initiationResponse) {
        return initiationResponse.getPollURL();
    }

    @Override
    protected String getInitialReference(CodeAppEntity initiationResponse) {
        return initiationResponse.getToken();
    }

    @Override
    protected void finalizeAuthentication() {}

    private <T> T decryptOtpChallenge(String username, String otpChallenge, Class<T> clazz) {
        driver =
                ChromeDriverInitializer.constructChromeDriver(
                        ChromeDriverConfig.builder()
                                .userAgent(DanskeBankConstants.Javascript.USER_AGENT)
                                .build(),
                        agentTemporaryStorage);
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createChallengeJavascript(
                            this.bindChallengeResponseBody, username, otpChallenge));

            // The JavaScript populate the DOM element body->challengeInfo with the decrypted
            // result.
            String decryptedChallenge =
                    driver.findElement(By.tagName("body")).getAttribute("challengeInfo");
            return DanskeBankDeserializer.convertStringToObject(decryptedChallenge, clazz);
        } catch (Exception e) {
            agentTemporaryStorage.remove(driver.getDriverId());
            throw e;
        }
    }

    public WebDriverWrapper getDriver() {
        return driver;
    }
}
