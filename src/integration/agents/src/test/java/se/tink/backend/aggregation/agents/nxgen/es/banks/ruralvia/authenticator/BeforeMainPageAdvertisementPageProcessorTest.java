package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.authenticator;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorageImpl;
import se.tink.backend.aggregation.wiremock.WireMockIntegrationTest;
import se.tink.integration.webdriver.ChromeDriverConfig;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.WebDriverWrapper;

public class BeforeMainPageAdvertisementPageProcessorTest extends WireMockIntegrationTest {

    private WebDriverWrapper driver;

    private AgentTemporaryStorage agentTemporaryStorage = new AgentTemporaryStorageImpl();

    private BeforeMainPageAdvertisementPageProcessor objectUnderTest;

    @Before
    public void init() {
        driver =
                ChromeDriverInitializer.constructChromeDriver(
                        ChromeDriverConfig.builder().build(), agentTemporaryStorage);
        objectUnderTest = new BeforeMainPageAdvertisementPageProcessor(driver);
        stubAllCssRequestsWithEmptyResponse();
        stubAllJavaScriptRequestsWithEmptyResponse();
    }

    @Test
    public void shouldJumpToGlobalPositionPageByClickingOnJumpLink() throws IOException {
        // given
        String responseBody =
                FileUtils.readFileToString(
                                Paths.get(
                                                RulaviaTestConstants.TEST_DATA_PATH,
                                                "beforeMainPageAdvertisementPage.html")
                                        .toFile(),
                                StandardCharsets.UTF_8)
                        .replace("https://www.ruralvia.com", getOrigin());
        ;
        String globalPositionBody =
                FileUtils.readFileToString(
                        Paths.get(RulaviaTestConstants.TEST_DATA_PATH, "globalPositionPage.html")
                                .toFile(),
                        StandardCharsets.UTF_8);
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/beforeMainPageAdvertisementPage.html"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody(responseBody)
                                        .withHeader("Content-Type", "text/html")));
        final String globalPositionUrlPath =
                "/isum/srv.BDP_RVIA05_ORDEN_INICIO_POSICION_GLOBAL_MULTI_PAR.BDP_RVIA05_CARRUSEL_POS_GLOBAL_MULTI";
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo(globalPositionUrlPath))
                        .willReturn(WireMock.aResponse().withBody(globalPositionBody)));
        driver.get(getOrigin() + "/beforeMainPageAdvertisementPage.html");

        // when
        AuthenticationStepResponse response = objectUnderTest.process();

        // then
        Assertions.assertThat(driver.getCurrentUrl())
                .isEqualTo(getOrigin() + globalPositionUrlPath);
        Assertions.assertThat(response.isExecuteNextStepInRow()).isTrue();
    }

    @Test
    public void shouldDoNothingWhenTheJumpLinkIsNotPresent() throws IOException {
        // given
        String globalPositionBody =
                FileUtils.readFileToString(
                        Paths.get(RulaviaTestConstants.TEST_DATA_PATH, "globalPositionPage.html")
                                .toFile(),
                        StandardCharsets.UTF_8);
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/globalPositionPage.html"))
                        .willReturn(WireMock.aResponse().withBody(globalPositionBody)));
        driver.get(getOrigin() + "/globalPositionPage.html");

        // then
        AuthenticationStepResponse response = objectUnderTest.process();

        // then
        Assertions.assertThat(response.isExecuteNextStepInRow()).isTrue();
    }
}
