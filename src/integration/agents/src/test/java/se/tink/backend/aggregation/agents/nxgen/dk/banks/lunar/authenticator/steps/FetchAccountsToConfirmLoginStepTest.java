package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import agents_platform_framework.org.springframework.http.HttpStatus;
import agents_platform_framework.org.springframework.web.server.ResponseStatusException;
import java.nio.file.Paths;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.LunarTestUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.client.AuthenticationApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessState;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class FetchAccountsToConfirmLoginStepTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/lunar/resources";

    private static final String DEVICE_ID = "some test id";
    private static final String ACCESS_TOKEN = "test_token";
    private static final String CHALLENGE = "1234567890123";

    private FetchAccountsToConfirmLoginStep fetchAccountsToConfirmLoginStep;
    private AuthenticationApiClient apiClient;
    private AgentProceedNextStepAuthenticationRequest request;
    private LunarProcessStateAccessor stateAccessor;
    private LunarProcessState processState;

    @Before
    public void setup() {
        LunarDataAccessorFactory dataAccessorFactory =
                new LunarDataAccessorFactory(new ObjectMapperFactory().getInstance());
        apiClient = mock(AuthenticationApiClient.class);
        fetchAccountsToConfirmLoginStep =
                new FetchAccountsToConfirmLoginStep(dataAccessorFactory, apiClient);

        processState = new LunarProcessState();
        processState.setChallenge(CHALLENGE);

        LunarAuthData initialData = new LunarAuthData();
        initialData.setAccessToken(ACCESS_TOKEN);
        initialData.setDeviceId(DEVICE_ID);

        stateAccessor = LunarTestUtils.getProcessStateAccessor(dataAccessorFactory, processState);
        LunarAuthDataAccessor authDataAccessor =
                LunarTestUtils.getAuthDataAccessor(dataAccessorFactory, initialData);

        request =
                LunarTestUtils.getProceedNextStepAuthRequest(
                        stateAccessor, authDataAccessor, processState, initialData);
    }

    @Test
    public void shouldFetchAccountsToConfirmLoginAndReturnSucceededResult() {
        // given
        AccountsResponse accountsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "accounts_response.json").toFile(),
                        AccountsResponse.class);
        when(apiClient.fetchAccounts(ACCESS_TOKEN, DEVICE_ID)).thenReturn(accountsResponse);

        // and
        LunarAuthData expectedData = new LunarAuthData();
        expectedData.setAccessToken(ACCESS_TOKEN);
        expectedData.setDeviceId(DEVICE_ID);
        expectedData.setAccountsResponse(accountsResponse);

        // when
        AgentAuthenticationResult result = fetchAccountsToConfirmLoginStep.execute(request);

        // then
        assertThat(result)
                .isEqualTo(
                        new AgentSucceededAuthenticationResult(
                                LunarTestUtils.toPersistedData(expectedData)));
    }

    @Test
    @Parameters(method = "failedFetchingParams")
    public void shouldReturnFailedResultWhenFetchingOfAccountsFailed(
            boolean isAutoAuth, AgentBankApiError error) {
        // given
        when(apiClient.fetchAccounts(ACCESS_TOKEN, DEVICE_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        // and
        setAutoAuth(isAutoAuth);

        // and
        AgentFailedAuthenticationResult expected =
                new AgentFailedAuthenticationResult(
                        error, LunarTestUtils.toPersistedData(new LunarAuthData()));

        // when
        AgentFailedAuthenticationResult result =
                (AgentFailedAuthenticationResult) fetchAccountsToConfirmLoginStep.execute(request);

        // then
        LunarTestUtils.assertFailedResultEquals(expected, result);
    }

    private Object[] failedFetchingParams() {
        return new Object[] {
            new Object[] {true, new SessionExpiredError()},
            new Object[] {false, new AuthorizationError()},
        };
    }

    private void setAutoAuth(boolean isAutoAuth) {
        processState.setAutoAuth(isAutoAuth);
        stateAccessor.storeState(processState);
    }

    @Test
    @Parameters(method = "nullAccountsResponseParams")
    public void shouldReturnFailedResultWhenTokenIsEmpty(
            boolean isAutoAuth, AgentBankApiError error) {
        // given
        when(apiClient.fetchAccounts(ACCESS_TOKEN, DEVICE_ID)).thenReturn(null);

        // and
        setAutoAuth(isAutoAuth);

        // and
        AgentFailedAuthenticationResult expected =
                new AgentFailedAuthenticationResult(
                        error, LunarTestUtils.toPersistedData(new LunarAuthData()));

        // when
        AgentFailedAuthenticationResult result =
                (AgentFailedAuthenticationResult) fetchAccountsToConfirmLoginStep.execute(request);

        // then
        LunarTestUtils.assertFailedResultEquals(expected, result);
    }

    private Object[] nullAccountsResponseParams() {
        return new Object[] {
            new Object[] {true, new SessionExpiredError()},
            new Object[] {false, new AuthorizationError()},
        };
    }
}
