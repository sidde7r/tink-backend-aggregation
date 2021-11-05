package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration.MarketConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.configuration.EIdasTinkCert;
import se.tink.backend.aggregation.nxgen.controllers.utils.ProviderSessionCacheController;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.wiremock.WireMockIntegrationTestServer;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RunWith(MockitoJUnitRunner.class)
public class IngBaseApiClientIntegrationTest {

    private static final WireMockIntegrationTestServer WIREMOCK_TEST_SERVER =
            new WireMockIntegrationTestServer();

    private PersistentStorage persistentStorage;

    @Mock private MarketConfiguration marketConfiguration;

    @Mock private AgentComponentProvider agentComponentProvider;

    private IngBaseApiClient baseApiClient;

    private final AgentConfiguration<IngBaseConfiguration> agentConfiguration =
            new AgentConfiguration.Builder<IngBaseConfiguration>()
                    .setRedirectUrl("https://non.existing.address.com")
                    .setQsealc(EIdasTinkCert.QSEALC)
                    .setQwac(EIdasTinkCert.QWAC)
                    .build();

    @Before
    public void setUp() throws CertificateException {
        defaultMockSetUp();

        baseApiClient =
                new IngBaseApiClient(
                        WIREMOCK_TEST_SERVER.createTinkHttpClient(),
                        persistentStorage = new PersistentStorage(),
                        mock(ProviderSessionCacheController.class),
                        marketConfiguration,
                        mock(QsealcSigner.class),
                        IngApiInputData.builder()
                                .userAuthenticationData(
                                        new IngUserAuthenticationData(true, "psuIpAddress"))
                                .credentialsRequest(mock(CredentialsRequest.class))
                                .build(),
                        agentComponentProvider);

        baseApiClient.setConfiguration(agentConfiguration);
    }

    @AfterClass
    public static void cleanUpClass() {
        WIREMOCK_TEST_SERVER.shutdown();
    }

    private void defaultMockSetUp() {
        when(marketConfiguration.marketCode()).thenReturn("AA");

        when(agentComponentProvider.getRandomValueGenerator())
                .thenReturn(new MockRandomValueGenerator());
        when(agentComponentProvider.getLocalDateTimeSource())
                .thenReturn(new ConstantLocalDateTimeSource());
    }

    @Test
    public void shouldReturnAuthorisedUrlBasedOnMarketParameter() {

        // given
        givenScenario("get_authorization_url.aap");

        // when
        URL authorisationUrl = baseApiClient.getAuthorizeUrl("my_test_state");

        // then
        assertThat(authorisationUrl).isNotNull();
        assertThat(persistentStorage).containsKeys("CLIENT_ID", "APPLICATION_TOKEN");
    }

    private static void givenScenario(String fileName) {
        WIREMOCK_TEST_SERVER.loadScenario(resource(fileName));
    }

    private static File resource(String fileName) {
        return Paths.get(
                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ingbase/resources")
                .resolve(fileName)
                .toFile();
    }
}
