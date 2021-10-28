package se.tink.backend.aggregation.wiremock;

import static se.tink.backend.aggregation.logmasker.LogMasker.LoggingMode.LOGGING_MASKER_COVERS_SECRETS;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.Set;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.RequestResponseParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.nxgen.http.IntegrationWireMockTestTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

/**
 * Helper class for REST client testing. Encapsulates WireMock server and pre-configured {@link
 * se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient}. One can check alternative approach
 * which use {@link se.tink.backend.aggregation.wiremock.WireMockIntegrationTest}
 *
 * @see se.tink.backend.aggregation.wiremock.WireMockIntegrationTest
 */
public class WireMockIntegrationTestServer {

    private final WireMockTestServer wireMockServer;

    public WireMockIntegrationTestServer() {
        wireMockServer = new WireMockTestServer(false);
    }

    public void loadScenario(File aapFile) {
        wireMockServer.loadRequestResponsePairs(singleAapFileParser(aapFile));
    }

    public TinkHttpClient createTinkHttpClient() {
        TinkHttpClient httpClient =
                NextGenTinkHttpClient.builder(new FakeLogMasker(), LOGGING_MASKER_COVERS_SECRETS)
                        .build();

        httpClient.disableSslVerification();

        return new IntegrationWireMockTestTinkHttpClient(
                httpClient, String.format("localhost:%s", wireMockServer.getHttpPort()), "http");
    }

    public void shutdown() {
        wireMockServer.shutdown();
    }

    private static RequestResponseParser aapFileParser(File aapFile) {
        return new AapFileParser(ResourceFileReader.read(aapFile));
    }

    private static Set<RequestResponseParser> singleAapFileParser(File aapFile) {
        return ImmutableSet.of(aapFileParser(aapFile));
    }
}
