package se.tink.backend.aggregation.wiremock;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
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

    private final TinkHttpClient tinkHttpClient;

    public WireMockIntegrationTestServer(File scenarioFile) {
        wireMockServer = createWireMockServer(scenarioFile);
        tinkHttpClient = createTinkHttpClient();
    }

    private WireMockTestServer createWireMockServer(File fileInAapFormat) {
        return new WireMockTestServer(
                ImmutableSet.of(
                        new AapFileParser(ResourceFileReader.read(fileInAapFormat.toString()))));
    }

    private TinkHttpClient createTinkHttpClient() {
        TinkHttpClient httpClient =
                NextGenTinkHttpClient.builder(
                                new FakeLogMasker(),
                                LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();

        httpClient.disableSslVerification();

        return new IntegrationWireMockTestTinkHttpClient(
                httpClient, String.format("localhost:%s", wireMockServer.getHttpPort()), "http");
    }

    public TinkHttpClient getTinkHttpClient() {
        return tinkHttpClient;
    }

    public void shutdown() {
        wireMockServer.shutdown();
    }
}
