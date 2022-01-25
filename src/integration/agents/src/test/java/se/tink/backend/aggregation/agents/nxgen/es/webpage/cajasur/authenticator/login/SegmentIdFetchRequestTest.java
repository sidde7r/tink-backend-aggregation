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

public class SegmentIdFetchRequestTest extends WireMockIntegrationTest {

    private SessionStorage sessionStorage = new SessionStorage();

    @Test
    public void shouldOpenPortalHomePage() throws IOException {
        // given
        String responseBody =
                FileUtils.readFileToString(
                        Paths.get(
                                        CajasurTestConstants.TEST_DATA_PATH,
                                        "portal_home_page_response_body.html")
                                .toFile(),
                        StandardCharsets.UTF_8);
        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/cs/Satellite/cajasur/es/particulares-0"))
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
        SegmentIdFetchRequest objectUnderTest = new SegmentIdFetchRequest(getOrigin());

        // when
        String segmentId = objectUnderTest.call(httpClient, sessionStorage);

        // then
        Assertions.assertThat(segmentId).isEqualTo("1298549581011");
    }
}
