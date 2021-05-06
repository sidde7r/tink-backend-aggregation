package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.fetchseedposition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.http.HttpStatus;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.ProcessDataUtil;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.mobileapp.SecurityNumberSeedResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessState;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.common.error.UnknownError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AuthenticationPersistedDataCookieStoreAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@RunWith(MockitoJUnitRunner.class)
public class FetchSeedPositionCallTest {
    private static final String USER_ID = "user_id";

    private static final ResponseEntity<String> SUCCESS =
            ResponseEntity.status(HttpStatus.OK)
                    .body(
                            "{\"accountCardLocked\":false,\"deviceSlotAvailable\":true,\"ibId\":\"***\",\"magicWordLocked\":false,\"seed\":\"1,4,8\",\"status\":\"IB_REGISTERED\"}");

    private static final ResponseEntity<String> INVALID_USER_DETAILS =
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            "{\"code\":\"INVALID_USER_DETAILS\",\"message\":\"The Customer ID or Username you have entered was not recognised. Please try again.\",\"messageType\":\"INLINE\"}");

    private static final ResponseEntity<String> UNKNOWN_ERROR =
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{}");

    @Mock private AgentHttpClient httpClient;

    private FetchSeedPositionCall call;

    @Before
    public void setUp() throws Exception {
        ObjectMapperFactory objectMapperFactory = new ObjectMapperFactory();
        this.call = new FetchSeedPositionCall(httpClient, objectMapperFactory.getInstance());
    }

    @Test
    public void shouldReturnProperValue() {
        // given
        FetchSeedPositionParameters parameters = new FetchSeedPositionParameters(USER_ID);
        AgentProceedNextStepAuthenticationRequest request =
                ProcessDataUtil.nextStepAuthRequest(
                        MetroProcessState::new, MetroAuthenticationData::new);
        when(httpClient.exchange(
                        call.prepareRequest(parameters, request.getAgentExtendedClientInfo()),
                        String.class,
                        request.getAgentExtendedClientInfo()))
                .thenReturn(SUCCESS);

        // when
        ExternalApiCallResult<SecurityNumberSeedResponse> execute =
                call.execute(
                        parameters,
                        request.getAgentExtendedClientInfo(),
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                request.getAuthenticationPersistedData()));

        // then
        assertThat(execute.getResponse()).isNotEmpty();
        assertThat(execute.getResponse().get().indexPositions()).isEqualTo(Arrays.asList(1, 4, 8));
    }

    @Test
    public void shouldCatchInvalidCredentials() {
        // given
        FetchSeedPositionParameters parameters = new FetchSeedPositionParameters(USER_ID);
        AgentProceedNextStepAuthenticationRequest request =
                ProcessDataUtil.nextStepAuthRequest(
                        MetroProcessState::new, MetroAuthenticationData::new);
        when(httpClient.exchange(
                        call.prepareRequest(parameters, request.getAgentExtendedClientInfo()),
                        String.class,
                        request.getAgentExtendedClientInfo()))
                .thenReturn(INVALID_USER_DETAILS);

        // when
        ExternalApiCallResult<SecurityNumberSeedResponse> execute =
                call.execute(
                        parameters,
                        request.getAgentExtendedClientInfo(),
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                request.getAuthenticationPersistedData()));

        // then
        assertThat(execute.getResponse()).isEmpty();
        assertThat(execute.getAgentBankApiError()).isNotEmpty();
        assertThat(execute.getAgentBankApiError().get())
                .isInstanceOf(InvalidCredentialsError.class);
    }

    @Test
    public void shouldCatchUnknownError() {
        // given
        FetchSeedPositionParameters parameters = new FetchSeedPositionParameters(USER_ID);
        AgentProceedNextStepAuthenticationRequest request =
                ProcessDataUtil.nextStepAuthRequest(
                        MetroProcessState::new, MetroAuthenticationData::new);
        when(httpClient.exchange(
                        call.prepareRequest(parameters, request.getAgentExtendedClientInfo()),
                        String.class,
                        request.getAgentExtendedClientInfo()))
                .thenReturn(UNKNOWN_ERROR);

        // when
        ExternalApiCallResult<SecurityNumberSeedResponse> execute =
                call.execute(
                        parameters,
                        request.getAgentExtendedClientInfo(),
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                request.getAuthenticationPersistedData()));

        // then
        assertThat(execute.getResponse()).isEmpty();
        assertThat(execute.getAgentBankApiError()).isNotEmpty();
        assertThat(execute.getAgentBankApiError().get()).isInstanceOf(UnknownError.class);
    }
}
