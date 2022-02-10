package se.tink.backend.aggregation.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

/**
 * Base class for WireMock integration testing. Useful for testing of REST client classes for
 * example . Requires manual stub configuration within a test. One can check alternative approach
 * which use {@link se.tink.backend.aggregation.wiremock.WireMockIntegrationTestServer}
 *
 * @see se.tink.backend.aggregation.wiremock.WireMockIntegrationTestServer
 */
@SuppressWarnings("java:S2187")
public class WireMockIntegrationTest {

    protected static final String PROTOCOL_AND_HOST = "http://127.0.0.1";

    @Rule public WireMockRule wireMock;
    protected TinkHttpClient httpClient;

    public WireMockIntegrationTest() {
        wireMock = new WireMockRule(WireMockConfiguration.options().dynamicPort());
        httpClient =
                NextGenTinkHttpClient.builder(
                                new FakeLogMasker(),
                                LogMasker.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();
    }

    protected String getOrigin() {
        return PROTOCOL_AND_HOST + ":" + wireMock.port();
    }

    protected void stubAllCssRequestsWithEmptyResponse() {
        WireMock.stubFor(
                WireMock.get(WireMock.urlMatching(".+\\.css.*"))
                        .willReturn(WireMock.aResponse().withBody("")));
    }

    protected void stubAllJavaScriptRequestsWithEmptyResponse() {
        WireMock.stubFor(
                WireMock.get(WireMock.urlMatching(".+\\.js.*"))
                        .willReturn(WireMock.aResponse().withBody("")));
    }
}
