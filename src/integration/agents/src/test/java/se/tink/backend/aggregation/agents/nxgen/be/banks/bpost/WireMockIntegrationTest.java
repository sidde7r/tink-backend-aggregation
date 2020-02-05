package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class WireMockIntegrationTest {

    private static final String PROTOCOL_AND_HOST = "http://127.0.0.1";
    protected static final int DEFAULT_PORT = 8089;
    private final int httpPort;

    @Rule public WireMockRule wireMock;
    protected TinkHttpClient httpClient;

    protected WireMockIntegrationTest() {
        this(DEFAULT_PORT);
    }

    public WireMockIntegrationTest(int httpPort) {
        this.httpPort = httpPort;
        wireMock = new WireMockRule(DEFAULT_PORT);
        httpClient =
                NextGenTinkHttpClient.builder(
                                LogMasker.builder().build(),
                                LogMasker.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();
    }

    protected String getOrigin() {
        return PROTOCOL_AND_HOST + ":" + httpPort;
    }
}
