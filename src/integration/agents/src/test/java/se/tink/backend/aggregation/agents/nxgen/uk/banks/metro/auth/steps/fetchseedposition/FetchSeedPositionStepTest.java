package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.fetchseedposition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.ProcessDataUtil;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.mobileapp.SecurityNumberSeedResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AuthenticationPersistedDataCookieStoreAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@RunWith(MockitoJUnitRunner.class)
public class FetchSeedPositionStepTest {

    private static final String USER_ID = "12345678901";

    @Mock private FetchSeedPositionCall fetcher;

    private FetchSeedPositionStep step;

    @Before
    public void setUp() throws Exception {
        MetroDataAccessorFactory dataAccessorFactory =
                new MetroDataAccessorFactory(new ObjectMapperFactory().getInstance());
        this.step = new FetchSeedPositionStep(dataAccessorFactory, fetcher);
    }

    @Test
    public void shouldFetchPositionsOfSecurityNumber() {
        // given
        AgentProceedNextStepAuthenticationRequest agentRequest =
                ProcessDataUtil.nextStepAuthRequest(
                        MetroProcessState::new,
                        () -> new MetroAuthenticationData().setUserId(USER_ID));
        String seed = "1,2,3";
        SecurityNumberSeedResponse response = new SecurityNumberSeedResponse();
        response.setSeed(seed);
        FetchSeedPositionParameters parameters = new FetchSeedPositionParameters(USER_ID);

        when(fetcher.execute(
                        parameters,
                        agentRequest.getAgentExtendedClientInfo(),
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                agentRequest.getAuthenticationPersistedData())))
                .thenReturn(new ExternalApiCallResult<>(response));

        // when
        AgentAuthenticationResult result = step.execute(agentRequest);

        // then
        assertThat(result).isInstanceOf(AgentProceedNextStepAuthenticationResult.class);
        assertThat(result.getAuthenticationProcessStepIdentifier().get().getValue())
                .isEqualTo("RegisterDeviceStep");
    }

    @Test
    public void shouldReturnAgentFailedAuthenticationResultWhenCallHasFailed() {
        // given
        AgentProceedNextStepAuthenticationRequest agentRequest =
                ProcessDataUtil.nextStepAuthRequest(
                        MetroProcessState::new,
                        () -> new MetroAuthenticationData().setUserId(USER_ID));
        FetchSeedPositionParameters parameters = new FetchSeedPositionParameters(USER_ID);
        when(fetcher.execute(
                        parameters,
                        agentRequest.getAgentExtendedClientInfo(),
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                agentRequest.getAuthenticationPersistedData())))
                .thenReturn(new ExternalApiCallResult<>(new InvalidCredentialsError()));

        // when
        AgentAuthenticationResult result = step.execute(agentRequest);

        // then
        assertThat(result).isInstanceOf(AgentFailedAuthenticationResult.class);
        assertThat(result.getAuthenticationProcessStepIdentifier()).isEmpty();
    }
}
