package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

@Ignore
public class HandelsbankenSEContentTypeFilterTest {

    private HandelsbankenSEApiClient apiClient;

    @Before
    public void setUp() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setField(Field.Key.USERNAME, "199001010000");

        AgentContext context = new AgentTestContext(credentials);
        TinkHttpClient client =
                new TinkHttpClient(
                        context.getAggregatorInfo(),
                        context.getMetricRegistry(),
                        context.getLogOutputStream(),
                        null);
        client.setDebugOutput(true);
        client.addFilter(new HandelsbankenSEContentTypeFilter());

        apiClient = new HandelsbankenSEApiClient(client, new HandelsbankenSEConfiguration());
    }

    @Test
    public void testHandlesEmptyContentType() {
        ApplicationEntryPointResponse entryPoint = mock(ApplicationEntryPointResponse.class);
        when(entryPoint.toKeepAlive()).thenReturn(new URL("http://localhost:3002/keepalive"));

        apiClient.keepAlive(entryPoint);
    }
}
