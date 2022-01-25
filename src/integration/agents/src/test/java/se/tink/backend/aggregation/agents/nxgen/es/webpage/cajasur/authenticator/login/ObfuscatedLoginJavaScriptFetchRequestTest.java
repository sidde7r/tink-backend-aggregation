package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurTestConstants;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.wiremock.WireMockIntegrationTest;

public class ObfuscatedLoginJavaScriptFetchRequestTest extends WireMockIntegrationTest {

    private SessionStorage sessionStorage = new SessionStorage();

    @Test
    public void shouldBuildExpectedJavaScriptContext() throws IOException {
        // given
        String responseBody =
                FileUtils.readFileToString(
                        Paths.get(
                                        CajasurTestConstants.TEST_DATA_PATH,
                                        "obfuscated_login_response_body.js")
                                .toFile(),
                        StandardCharsets.UTF_8);
        WireMock.stubFor(
                WireMock.get(
                                WireMock.urlPathEqualTo(
                                        "/internetcs/js/login/encriptarLoginOfuscado.js"))
                        .withHeader("Accept-Encoding", WireMock.equalTo("gzip, deflate"))
                        .withHeader("Connection", WireMock.equalTo("keep-alive"))
                        .withHeader(
                                "Accept-Language",
                                WireMock.equalTo("es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3"))
                        .withHeader("Accept", WireMock.containing("text/html"))
                        .withHeader("Accept", WireMock.containing("application/xhtml+xml"))
                        .withHeader("Keep-Alive", WireMock.equalTo("300"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody(responseBody)
                                        .withHeader("Content-Type", "text/html;charset=UTF-8")));
        ObfuscatedLoginJavaScriptFetchRequest objectUnderTest =
                new ObfuscatedLoginJavaScriptFetchRequest(getOrigin());

        // when
        String result = objectUnderTest.call(httpClient, sessionStorage);

        // then
        Assertions.assertThat(result).startsWith("var _0x2a98=");
    }
}
