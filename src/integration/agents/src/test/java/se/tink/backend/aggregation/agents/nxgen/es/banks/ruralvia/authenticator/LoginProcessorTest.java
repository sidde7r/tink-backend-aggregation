package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.authenticator;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorageImpl;
import se.tink.backend.aggregation.wiremock.WireMockIntegrationTest;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.integration.webdriver.ChromeDriverConfig;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.WebDriverWrapper;

@RunWith(MockitoJUnitRunner.class)
public class LoginProcessorTest extends WireMockIntegrationTest {

    private WebDriverWrapper driver;

    private AgentTemporaryStorage agentTemporaryStorage = new AgentTemporaryStorageImpl();

    @Mock private Credentials credentials;

    private LoginProcessor objectUnderTest;

    @Before
    public void init() {
        driver =
                ChromeDriverInitializer.constructChromeDriver(
                        ChromeDriverConfig.builder().build(), agentTemporaryStorage);
        objectUnderTest = new LoginProcessor(driver, credentials);
        stubAllCssRequestsWithEmptyResponse();
    }

    @Test
    public void shouldThrowCredentialsIncorrectWhenContainsIllegalCharacters() throws IOException {
        // given
        final String resourceFileName = "loginForm.html";
        String loginFormBody =
                FileUtils.readFileToString(
                        Paths.get(RulaviaTestConstants.TEST_DATA_PATH, resourceFileName).toFile(),
                        StandardCharsets.UTF_8);
        String checkUsuPassJS =
                FileUtils.readFileToString(
                        Paths.get(RulaviaTestConstants.TEST_DATA_PATH, "checkUsuPass.js").toFile(),
                        StandardCharsets.UTF_8);
        WireMock.stubFor(
                WireMock.get(WireMock.urlMatching("/" + resourceFileName))
                        .willReturn(WireMock.aResponse().withBody(loginFormBody)));
        WireMock.stubFor(
                WireMock.get(WireMock.urlMatching("/javascripts_portal/.+\\.js.*"))
                        .willReturn(WireMock.aResponse().withBody("")));
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/js/redireccion.js"))
                        .willReturn(WireMock.aResponse().withBody("")));
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/js/checkUsuPass.js"))
                        .willReturn(WireMock.aResponse().withBody(checkUsuPassJS)));
        driver.get(getOrigin() + "/" + resourceFileName);
        Mockito.when(credentials.getField(Field.Key.USERNAME)).thenReturn("fakeTestUsername");
        Mockito.when(credentials.getField(Field.Key.PASSWORD)).thenReturn("");
        Mockito.when(credentials.getField(Field.Key.NATIONAL_ID_NUMBER)).thenReturn("435435546");

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
    public void shouldFillAndSubmitLoginForm() throws IOException {
        // given
        final String resourceFileName = "loginForm.html";
        final String credentialsUsername = "fakeUser";
        final String credentialsNationalIdNumber = "435435546";
        final String credenetialsPassword = "fakeTestPassword";
        stubAllJavaScriptRequestsWithEmptyResponse();
        String loginFormBody =
                FileUtils.readFileToString(
                                Paths.get(RulaviaTestConstants.TEST_DATA_PATH, resourceFileName)
                                        .toFile(),
                                StandardCharsets.UTF_8)
                        .replace("https://www.ruralvia.com:443", getOrigin());
        WireMock.stubFor(
                WireMock.get(WireMock.urlMatching("/" + resourceFileName))
                        .willReturn(WireMock.aResponse().withBody(loginFormBody)));
        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo("/isum/Main"))
                        .withQueryParam("ISUM_LM_REQUEST", WireMock.matching(".+"))
                        .withRequestBody(
                                WireMock.containing(
                                        String.format(
                                                "yMZYVDsIEHDfmMA=%s&xRjhwWVMbRuQiqQ=%s&DQRtYHqdKZuTWBE=%s",
                                                credentialsUsername.toUpperCase(),
                                                credentialsNationalIdNumber,
                                                credenetialsPassword)))
                        .willReturn(WireMock.aResponse()));
        driver.get(getOrigin() + "/" + resourceFileName);
        Mockito.when(credentials.getField(Field.Key.USERNAME)).thenReturn(credentialsUsername);
        Mockito.when(credentials.getField(Field.Key.PASSWORD)).thenReturn(credenetialsPassword);
        Mockito.when(credentials.getField(Field.Key.NATIONAL_ID_NUMBER))
                .thenReturn(credentialsNationalIdNumber);

        // when
        AuthenticationStepResponse response = objectUnderTest.process();

        // then
        Assertions.assertThat(response.isExecuteNextStepInRow()).isTrue();
        Assertions.assertThat(driver.getCurrentUrl())
                .isEqualTo(
                        getOrigin()
                                + "/isum/Main?ISUM_LM_REQUEST=6uOruSSXVU8aJvIzja7z5BlCYYN75HLDAVC84BXPcAuXziW3CquBDFpKL0dIqbQ31%2FFVrJBRudsn7MY3MStMk3FTRt78Bkpu0YKh%2BasI7bIENSlu2HJAuKv5WRmO6Z6OTNynsq2kVQnmGcsFWmJQqEVIV%2Bt9m1a2KXXcE%2Fkzw8ARjvBMXLzReWzG4sbX3An8wk0O0JylptAyS9bm42ClwlFenVclvuK60hisN6Zm1NRGNsYqxerv08BfS2b2jigleylFKTWVyfCdOZKTutN4gtIX7DH1qwqHWLI%2B6pmbwgNQnvbfd2wwnw3S9e5sBsiTuVMSS73Dx1%2FmxvMScPX31p01jnfxeefgk%2BHy4vVhCNw2mDB56%2F%2FVaH4YrrOuikpOV5yaU%2Fg5opdIowL90%2BRFZqwBXMt4z9dG5RjuQAV9O%2F%2BtFnUfr9LUVSfTHBXeuk8CIYo60z8qaWy8gtvdS67co5ZPno5ckMX88m4UhQJxfjfk%2B1clBklS1OXbMmqUXIO1D%2BpsSnjj28uJBZVBdkbRynK9fltDXwQhkTTcfnEVcS2N5R5UW4mnb2ILdSnuTxvHDw7GYp0T4TYV1dQ0mtqEntdrDI5eY3ocrAaRWOL05PT19Ijy4TKhkF1XQtGfClKmQwDWakWPP73qNBqnYbaSoYShhOphLKf60McIdAyGF8HWSYJN%2FFihxgwIPnFMaru%2B5tYpsa9CUfkuI5dKpqukscchB3KGtNrnJPiQHhMBBa8EEnPami4KHrAojjiTrHGwjy1bRq412Em7zJtcfF3bwkdrBstqhwy75StGexqSV%2FokTsCNvX3TIyks8MYQhH2feVozr5Az%2BT6tSX3KWpva6YrJ11SiRSvUrJkKmgwUCVFBfzp%2BxG53bgQgiBFoDZ4uG2jPcrnmppp80cLcJyPwuUm77URO5fcuZ94S1hgFV9qZgbvdSGnjE2Nz9CRPeGstI%2Fr%2B3pN26lISgO8IKSuHAYKYESns44G6bvapgR8sLZpAQn60G4qGT5KLoJx%2BOnKVXTFJZ3QnluyQ%2BR2MtqNDpet%2FOLKMngUzYgt7a%2BDkbpvps0gA0GLZ%2FW6YT3TCx13vZrsMKKSAIVjBewtgm%2BnMzF9X8r2UOTalZkx1MxUPgRZuZVTheRVquncSN%2BOuJAicDH4kdPsiTVj1tnviC3jUvdQVDfJpgH2OmKp4fat8Leedn7tGqDrMooe9PdVUxXXo");
    }
}
