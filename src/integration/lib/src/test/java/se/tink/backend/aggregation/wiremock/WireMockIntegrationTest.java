package se.tink.backend.aggregation.wiremock;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class WireMockIntegrationTest {

    protected static final String PROTOCOL_AND_HOST = "http://127.0.0.1";

    @Rule public WireMockRule wireMock;
    protected TinkHttpClient httpClient;

    public WireMockIntegrationTest() {
        wireMock = new WireMockRule(WireMockConfiguration.options().dynamicPort());
        httpClient =
                NextGenTinkHttpClient.builder(
                                LogMaskerImpl.builder().build(),
                                LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();
    }

    protected String getOrigin() {
        return PROTOCOL_AND_HOST + ":" + wireMock.port();
    }
}
