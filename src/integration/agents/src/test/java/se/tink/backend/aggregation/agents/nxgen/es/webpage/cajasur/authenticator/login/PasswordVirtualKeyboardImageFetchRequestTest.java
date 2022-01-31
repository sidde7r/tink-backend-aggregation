package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurTestConstants;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.wiremock.WireMockIntegrationTest;

public class PasswordVirtualKeyboardImageFetchRequestTest extends WireMockIntegrationTest {

    private SessionStorage sessionStorage = new SessionStorage();

    @Test
    public void shouldFetchPasswordVirtualKeyboardImage() throws IOException {
        // given
        byte[] responseBody =
                Files.readAllBytes(
                        Paths.get(
                                CajasurTestConstants.TEST_DATA_PATH, "virtual_keyboard_login.gif"));
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/NASApp/BesaideNet2/Gestor"))
                        .withQueryParam("PRESTACION", WireMock.equalTo("login"))
                        .withQueryParam("FUNCION", WireMock.equalTo("login"))
                        .withQueryParam("ACCION", WireMock.equalTo("directoportalImage"))
                        .withQueryParam("idioma", WireMock.equalTo("ES"))
                        .withHeader("Accept-Encoding", WireMock.equalTo("gzip, deflate"))
                        .withHeader("Connection", WireMock.equalTo("keep-alive"))
                        .withHeader(
                                "Accept-Language",
                                WireMock.equalTo("es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3"))
                        .withHeader("Accept", WireMock.containing("image/png"))
                        .withHeader("Accept", WireMock.containing("image/svg+xml"))
                        .withHeader("Accept", WireMock.containing("image/jxr"))
                        .withHeader("Keep-Alive", WireMock.equalTo("300"))
                        .willReturn(WireMock.aResponse().withBody(responseBody)));
        PasswordVirtualKeyboardImageFetchRequest objectUnderTest =
                new PasswordVirtualKeyboardImageFetchRequest(getOrigin());

        // when
        BufferedImage result = objectUnderTest.call(httpClient, sessionStorage);

        // then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getWidth()).isEqualTo(135);
        Assertions.assertThat(result.getHeight()).isEqualTo(51);
    }
}
