package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.configuration.CitadeleBaseConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CitadeleBaseApiClientTest {

    private CitadeleBaseApiClient apiClient;
    PersistentStorage persistentStorage = new PersistentStorage();
    private RequestBuilder requestBuilder;

    @Before
    public void setUp() {
        TinkHttpClient client = mock(TinkHttpClient.class);
        requestBuilder = mock(RequestBuilder.class);
        AgentConfiguration<CitadeleBaseConfiguration> agentConfiguration =
                mock(AgentConfiguration.class);

        apiClient =
                new CitadeleBaseApiClient(
                        client,
                        persistentStorage,
                        agentConfiguration,
                        mock(RandomValueGenerator.class),
                        mock(User.class),
                        LocalDate.now());
    }

    @Test
    public void shouldThrowSessionErrorExceptionCausedByLackOfConsentId() {
        assertThatThrownBy(() -> apiClient.getConsentStatus())
                .isInstanceOf(SessionError.SESSION_EXPIRED.exception().getClass());
    }
}
