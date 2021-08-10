package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.manual;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.backend.aggregation.agents.framework.context.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.filters.HandelsbankenSEContentTypeFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl.LoggingMode;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class HandelsbankenSEContentTypeFilterTest {

    private final ArgumentManager<UsernameArgumentEnum> helper =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    private HandelsbankenSEApiClient apiClient;

    @Before
    public void setUp() throws Exception {
        helper.before();
        Credentials credentials = new Credentials();
        credentials.setField(Field.Key.USERNAME, helper.get(UsernameArgumentEnum.USERNAME));

        AgentContext context = new AgentTestContext(credentials);
        TinkHttpClient client =
                new LegacyTinkHttpClient(
                        context.getAggregatorInfo(),
                        context.getMetricRegistry(),
                        context.getLogOutputStream(),
                        null,
                        null,
                        context.getLogMasker(),
                        LoggingMode.LOGGING_MASKER_COVERS_SECRETS);
        client.addFilter(new HandelsbankenSEContentTypeFilter());

        apiClient = new HandelsbankenSEApiClient(client, new HandelsbankenSEConfiguration());
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testHandlesEmptyContentType() {
        ApplicationEntryPointResponse entryPoint = mock(ApplicationEntryPointResponse.class);
        when(entryPoint.toKeepAlive()).thenReturn(new URL("http://localhost:3002/keepalive"));

        apiClient.keepAlive(entryPoint);
    }
}
