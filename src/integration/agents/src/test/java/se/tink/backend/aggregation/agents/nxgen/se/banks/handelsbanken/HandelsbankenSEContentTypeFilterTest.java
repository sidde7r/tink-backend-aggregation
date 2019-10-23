package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.log.LogMasker;
import se.tink.backend.aggregation.log.LogMasker.LoggingMode;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.ClientConfigurationStringMaskerBuilder;
import se.tink.backend.aggregation.utils.CredentialsStringMaskerBuilder;

@Ignore
public class HandelsbankenSEContentTypeFilterTest {

    private HandelsbankenSEApiClient apiClient;

    @Before
    public void setUp() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setField(Field.Key.USERNAME, "199001010000");

        AgentContext context = new AgentTestContext(credentials);
        TinkHttpClient client =
                new LegacyTinkHttpClient(
                        context.getAggregatorInfo(),
                        context.getMetricRegistry(),
                        context.getLogOutputStream(),
                        null,
                        null,
                        LogMasker.builder()
                                .addStringMaskerBuilder(
                                        new CredentialsStringMaskerBuilder(
                                                credentials,
                                                ImmutableList.of(
                                                        CredentialsStringMaskerBuilder
                                                                .CredentialsProperty.PASSWORD,
                                                        CredentialsStringMaskerBuilder
                                                                .CredentialsProperty.SECRET_KEY,
                                                        CredentialsStringMaskerBuilder
                                                                .CredentialsProperty
                                                                .SENSITIVE_PAYLOAD,
                                                        CredentialsStringMaskerBuilder
                                                                .CredentialsProperty.USERNAME)))
                                .addStringMaskerBuilder(
                                        new ClientConfigurationStringMaskerBuilder(
                                                context.getAgentConfigurationController()
                                                        .getSecretValues()))
                                .build(),
                        LoggingMode.LOGGING_MASKER_COVERS_SECRETS);
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
