package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.awt.image.BufferedImage;
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
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurTestConstants;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.virtualkeyboardocr.PasswordVirtualKeyboardOcr;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.virtualkeyboardocr.VirtualKeyboardImageParameters;
import se.tink.backend.aggregation.nxgen.http.CookieRepository;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.wiremock.WireMockIntegrationTest;

@RunWith(MockitoJUnitRunner.class)
public class LoginRequestTest extends WireMockIntegrationTest {

    private static final String USERNAME = "76884252J";
    private static final String PASSWORD = "000000";
    private static final String SEGMENT_ID = "1298549581011";

    @Mock private BufferedImage passwordVirtualKeyboard;

    @Mock private PasswordVirtualKeyboardOcr passwordVirtualKeyboardOcr;

    private SessionStorage sessionStorage = new SessionStorage();

    @Before
    public void init() {
        Mockito.when(
                        passwordVirtualKeyboardOcr.getNumbersSequenceFromImage(
                                passwordVirtualKeyboard,
                                PASSWORD,
                                VirtualKeyboardImageParameters.createEnterpriseConfiguration()))
                .thenReturn("092809");
        CookieRepository cookieRepository = CookieRepository.getInstance(sessionStorage);
        cookieRepository.addCookie("JSESSIONID", "324532345345");
        cookieRepository.save(sessionStorage);
    }

    @Test
    public void shouldCallForLogin() throws IOException {
        // given
        String obfuscatedLoginJS =
                FileUtils.readFileToString(
                        Paths.get(
                                        CajasurTestConstants.TEST_DATA_PATH,
                                        "obfuscated_login_response_body.js")
                                .toFile(),
                        StandardCharsets.UTF_8);
        String responseBody =
                FileUtils.readFileToString(
                        Paths.get(
                                        CajasurTestConstants.TEST_DATA_PATH,
                                        "login_success_response.html")
                                .toFile(),
                        StandardCharsets.UTF_8);
        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo("/NASApp/BesaideNet2/Gestor"))
                        .withQueryParam("PORTAL_CON_DCT", WireMock.equalTo("SI"))
                        .withQueryParam("PRESTACION", WireMock.equalTo("login"))
                        .withQueryParam("FUNCION", WireMock.equalTo("directoportal"))
                        .withQueryParam("ACCION", WireMock.equalTo("control"))
                        .withHeader("Accept-Encoding", WireMock.equalTo("gzip, deflate"))
                        .withHeader("Connection", WireMock.equalTo("keep-alive"))
                        .withHeader(
                                "Accept-Language",
                                WireMock.equalTo("es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3"))
                        .withHeader("Keep-Alive", WireMock.equalTo("300"))
                        .withHeader(
                                "Content-Type",
                                WireMock.equalTo("application/x-www-form-urlencoded"))
                        .withCookie("JSESSIONID", WireMock.matching(".+"))
                        .withRequestBody(
                                WireMock.containing(
                                        "idioma=ES&password=092809&tecladoVirtual=SI&usuarioInsertado=76884252J&usuarioSinFormatear=76884252J&activador=MP&sitioWeb=&destino=&tipoacceso=&idSegmento=1298549581011&DATA_LOGON_PORTAL=V01%"))
                        .willReturn(WireMock.aResponse().withBody(responseBody)));
        LoginRequest objectUnderTest =
                new LoginRequest(
                        getOrigin(),
                        passwordVirtualKeyboardOcr,
                        new LoginRequestParams(
                                USERNAME,
                                PASSWORD,
                                SEGMENT_ID,
                                obfuscatedLoginJS,
                                passwordVirtualKeyboard));

        // when
        String result = objectUnderTest.call(httpClient, sessionStorage);

        // then
        Assertions.assertThat(result).contains("name=\"frmLogin\"");
    }
}
