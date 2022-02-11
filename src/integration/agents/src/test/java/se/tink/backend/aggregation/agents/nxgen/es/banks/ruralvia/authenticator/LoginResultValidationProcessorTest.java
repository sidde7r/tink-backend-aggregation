package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.authenticator;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorageImpl;
import se.tink.backend.aggregation.wiremock.WireMockIntegrationTest;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.integration.webdriver.ChromeDriverConfig;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.WebDriverWrapper;

public class LoginResultValidationProcessorTest extends WireMockIntegrationTest {

    private WebDriverWrapper driver;

    private AgentTemporaryStorage agentTemporaryStorage = new AgentTemporaryStorageImpl();

    private LoginResultValidationProcessor objectUnderTest;

    @Before
    public void init() {
        driver =
                ChromeDriverInitializer.constructChromeDriver(
                        ChromeDriverConfig.builder().build(), agentTemporaryStorage);
        objectUnderTest = new LoginResultValidationProcessor(driver);
    }

    @Test
    public void shouldThrowUserBlockedException() throws IOException {
        // given
        final String resourceFileName = "authenticationFailedUserBlocked.html";
        String responseBody =
                FileUtils.readFileToString(
                        Paths.get(RulaviaTestConstants.TEST_DATA_PATH, resourceFileName).toFile(),
                        StandardCharsets.UTF_8);
        stubAllCssRequestsWithEmptyResponse();
        stubAllJavaScriptRequestsWithEmptyResponse();
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/" + resourceFileName))
                        .willReturn(WireMock.aResponse().withBody(responseBody)));

        driver.get(getOrigin() + "/" + resourceFileName);

        // when
        Throwable throwable = Assertions.catchThrowable(() -> objectUnderTest.process());

        // then
        Assertions.assertThat(throwable).isInstanceOf(ConnectivityException.class);
        Assertions.assertThat(
                        ((ConnectivityException) throwable).getError().getDetails().getReason())
                .isEqualTo(ConnectivityErrorDetails.UserLoginErrors.USER_BLOCKED.name());
    }

    @Test
    public void shouldThrowUserIncorrectCredentialsException() throws IOException {
        // given
        final String resourceFileName = "authenticationFailedIncorrectCredentials.html";
        String responseBody =
                FileUtils.readFileToString(
                        Paths.get(RulaviaTestConstants.TEST_DATA_PATH, resourceFileName).toFile(),
                        StandardCharsets.UTF_8);
        stubAllCssRequestsWithEmptyResponse();
        stubAllJavaScriptRequestsWithEmptyResponse();
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/" + resourceFileName))
                        .willReturn(WireMock.aResponse().withBody(responseBody)));

        driver.get(getOrigin() + "/" + resourceFileName);

        // when
        Throwable throwable = Assertions.catchThrowable(() -> objectUnderTest.process());

        // then
        Assertions.assertThat(throwable).isInstanceOf(ConnectivityException.class);
        Assertions.assertThat(
                        ((ConnectivityException) throwable).getError().getDetails().getReason())
                .isEqualTo(
                        ConnectivityErrorDetails.UserLoginErrors.STATIC_CREDENTIALS_INCORRECT
                                .name());
    }

    @Test
    public void shouldFindGlobalPageAndPassFlowToTheNextStep() throws IOException {
        // given
        final String resourceFileName = "globalPositionPage.html";
        String responseBody =
                FileUtils.readFileToString(
                        Paths.get(RulaviaTestConstants.TEST_DATA_PATH, resourceFileName).toFile(),
                        StandardCharsets.UTF_8);
        stubAllCssRequestsWithEmptyResponse();
        stubAllJavaScriptRequestsWithEmptyResponse();
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/" + resourceFileName))
                        .willReturn(WireMock.aResponse().withBody(responseBody)));

        driver.get(getOrigin() + "/" + resourceFileName);

        // when
        AuthenticationStepResponse response = objectUnderTest.process();

        // then
        Assertions.assertThat(response.isExecuteNextStepInRow()).isTrue();
    }
}
