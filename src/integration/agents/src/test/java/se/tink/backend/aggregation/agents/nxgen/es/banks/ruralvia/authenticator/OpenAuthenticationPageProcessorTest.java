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

public class OpenAuthenticationPageProcessorTest extends WireMockIntegrationTest {

    private WebDriverWrapper driver;

    private AgentTemporaryStorage agentTemporaryStorage = new AgentTemporaryStorageImpl();

    @Before
    public void init() {

        driver =
                ChromeDriverInitializer.constructChromeDriver(
                        ChromeDriverConfig.builder().build(), agentTemporaryStorage);
        stubAllCssRequestsWithEmptyResponse();
        stubAllJavaScriptRequestsWithEmptyResponse();
    }

    @Test
    public void shouldFindLoginPageLinkAndOpen() throws IOException {
        // given
        final String resourceFileName = "mainPage.html";
        String globalPositionBody =
                FileUtils.readFileToString(
                                Paths.get(RulaviaTestConstants.TEST_DATA_PATH, resourceFileName)
                                        .toFile(),
                                StandardCharsets.UTF_8)
                        .replace("https://www.ruralvia.com", getOrigin());
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/" + resourceFileName))
                        .willReturn(WireMock.aResponse().withBody(globalPositionBody)));
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/isum/Main"))
                        .withQueryParam("ISUM_SCR", WireMock.equalTo("login"))
                        .withQueryParam("loginType", WireMock.equalTo("accesoSeguro"))
                        .withQueryParam("ISUM_Portal", WireMock.equalTo("2"))
                        .withQueryParam("acceso_idioma", WireMock.equalTo("es_ES"))
                        .willReturn(WireMock.aResponse().withBody(globalPositionBody)));
        OpenAuthenticationPageProcessor objectUnderTest =
                new OpenAuthenticationPageProcessor(driver, getOrigin() + "/" + resourceFileName);

        // when
        AuthenticationStepResponse response = objectUnderTest.process();

        // then
        Assertions.assertThat(response.isExecuteNextStepInRow()).isTrue();
        Assertions.assertThat(driver.getCurrentUrl())
                .isEqualTo(
                        getOrigin()
                                + "/isum/Main?ISUM_SCR=login&loginType=accesoSeguro&ISUM_Portal=2&acceso_idioma=es_ES");
    }

    @Test
    public void shouldThrowTinkInternalErrorWhenHyperlinkToLoginPageIsMissing() {
        // given
        final String mainPagePath = "mainPage";
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/" + mainPagePath))
                        .willReturn(WireMock.aResponse().withBody("")));
        OpenAuthenticationPageProcessor objectUnderTest =
                new OpenAuthenticationPageProcessor(driver, getOrigin() + "/" + mainPagePath);

        // when
        Throwable throwable = Assertions.catchThrowable(() -> objectUnderTest.process());

        // then
        Assertions.assertThat(throwable).isInstanceOf(ConnectivityException.class);
        Assertions.assertThat(
                        ((ConnectivityException) throwable).getError().getDetails().getReason())
                .isEqualTo(
                        ConnectivityErrorDetails.TinkSideErrors.TINK_INTERNAL_SERVER_ERROR.name());
    }
}
