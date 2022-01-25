package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.mainview;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurSessionState;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurTestConstants;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.wiremock.WireMockIntegrationTest;

public class PostLoginFormSubmitRequestTest extends WireMockIntegrationTest {

    private SessionStorage sessionStorage = new SessionStorage();
    private CajasurSessionState sessionState = CajasurSessionState.getInstance(sessionStorage);

    @Test
    public void shouldReturnMainViewUrl() throws IOException {
        // given
        String loginResponseBody =
                FileUtils.readFileToString(
                        Paths.get(
                                        CajasurTestConstants.TEST_DATA_PATH,
                                        "login_success_response.html")
                                .toFile(),
                        StandardCharsets.UTF_8);
        sessionState.saveLoginResponse(loginResponseBody);

        String responseBody =
                FileUtils.readFileToString(
                        Paths.get(
                                        CajasurTestConstants.TEST_DATA_PATH,
                                        "submit_post_login_form_response.html")
                                .toFile(),
                        StandardCharsets.UTF_8);
        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo("/NASApp/BesaideNet2/Gestor"))
                        .withQueryParam("PRESTACION", WireMock.equalTo("login"))
                        .withQueryParam("FUNCION", WireMock.equalTo("vinculos"))
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
                        .withRequestBody(
                                WireMock.equalTo(
                                        "PORTAL_CON_DCT=SI&sitioWeb=&tecladoVirtual=SI&activador=MP&idioma=ES&DATA_LOGON_PORTAL=V01|0e866b8511acc70f886f0984eecd00b3|2c2c37b35e07255c5290911b9d98f92b|f3Rl2e6tlh9EZ9XTWaNd/A/PEGXjWNXfAuvnAqofooJCeuDvIw3ygWU0sxC5/y1K&idSegmento=1298549581011&tipoacceso=&destino="))
                        .willReturn(WireMock.aResponse().withBody(responseBody)));

        PostLoginFormSubmitRequest objectUnderTest =
                new PostLoginFormSubmitRequest(getOrigin(), sessionState);

        // when
        URL redirectUrl = objectUnderTest.call(httpClient, sessionStorage);

        // then
        Assertions.assertThat(redirectUrl)
                .isEqualTo(
                        new URL(
                                getOrigin()
                                        + "/NASApp/BesaideNet2/pages/login/entradaBanca.iface?destino=resumen.home"));
    }
}
