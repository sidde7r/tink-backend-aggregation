package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.mainview;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurTestConstants;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.wiremock.WireMockIntegrationTest;

public class MainViewRequestTest extends WireMockIntegrationTest {

    @Test
    public void shouldReturnMainPageHtmlBody() throws IOException {
        // given
        String mainPageResponseBody =
                FileUtils.readFileToString(
                        Paths.get(
                                        CajasurTestConstants.TEST_DATA_PATH,
                                        "portal_home_page_response_body.html")
                                .toFile(),
                        StandardCharsets.UTF_8);

        WireMock.stubFor(
                WireMock.get(
                                WireMock.urlPathEqualTo(
                                        "/NASApp/BesaideNet2/pages/login/entradaBanca.iface"))
                        .withQueryParam("destino", WireMock.equalTo("resumen.home"))
                        .withHeader("Accept-Encoding", WireMock.equalTo("gzip, deflate"))
                        .withHeader("Connection", WireMock.equalTo("keep-alive"))
                        .withHeader(
                                "Accept-Language",
                                WireMock.equalTo("es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3"))
                        .withHeader("Keep-Alive", WireMock.equalTo("300"))
                        .willReturn(WireMock.aResponse().withBody(mainPageResponseBody)));

        MainViewRequest objectUnderTest =
                new MainViewRequest(
                        new URL(
                                getOrigin()
                                        + "/NASApp/BesaideNet2/pages/login/entradaBanca.iface?destino=resumen.home"));

        // when
        String result = objectUnderTest.call(httpClient);

        // then
        Assertions.assertThat(result).contains("Bienvenidos al Portal de CajaSur");
    }
}
