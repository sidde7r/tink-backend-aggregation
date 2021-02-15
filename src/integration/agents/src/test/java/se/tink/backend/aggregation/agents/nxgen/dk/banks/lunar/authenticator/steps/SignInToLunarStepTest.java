package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import agents_platform_framework.org.springframework.http.HttpStatus;
import agents_platform_framework.org.springframework.web.server.ResponseStatusException;
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
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccessTokenFetchingFailureError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;

@RunWith(JUnitParamsRunner.class)
public class SignInToLunarStepTest {

    private static final String DEVICE_ID = "some test id";
    private static final String LUNAR_PASSWORD = "1234";
    private static final String ACCESS_TOKEN = "test_token";
    private static final String CHALLENGE = "1234567890123";

    private SignInToLunarStep signInToLunarStep;
    private AuthenticationApiClient apiClient;
    private AgentProceedNextStepAuthenticationRequest request;
    private LunarProcessStateAccessor stateAccessor;
    private LunarProcessState processState;

    @Before
    public void setup() {
        LunarDataAccessorFactory dataAccessorFactory =
                new LunarDataAccessorFactory(new ObjectMapperFactory().getInstance());
        apiClient = mock(AuthenticationApiClient.class);
        signInToLunarStep = new SignInToLunarStep(dataAccessorFactory, apiClient);

        processState = new LunarProcessState();
        processState.setChallenge(CHALLENGE);

        LunarAuthData initialData = new LunarAuthData();
        initialData.setLunarPassword(LUNAR_PASSWORD);
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
    @Parameters({ACCESS_TOKEN, "new token"})
    public void shouldSignInToLunarAndReturnSucceededResult(String token) {
        // given
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setToken(token);
        when(apiClient.signIn(LUNAR_PASSWORD, ACCESS_TOKEN, DEVICE_ID)).thenReturn(tokenResponse);

        // and
        LunarAuthData expectedData = new LunarAuthData();
        expectedData.setLunarPassword(LUNAR_PASSWORD);
        expectedData.setAccessToken(token);
        expectedData.setDeviceId(DEVICE_ID);

        // when
        AgentAuthenticationResult result = signInToLunarStep.execute(request);

        // then
        assertThat(result)
                .isEqualTo(
                        new AgentSucceededAuthenticationResult(
                                LunarTestUtils.toPersistedData(expectedData)));
    }

    @Test
    @Parameters(method = "failedSignInRequestParams")
    public void shouldReturnFailedResultWhenSignInRequestFailed(
            boolean isAutoAuth, AgentBankApiError error) {
        // given
        when(apiClient.signIn(LUNAR_PASSWORD, ACCESS_TOKEN, DEVICE_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        // and
        setAutoAuth(isAutoAuth);

        // and
        AgentFailedAuthenticationResult expected =
                new AgentFailedAuthenticationResult(
                        error, LunarTestUtils.toPersistedData(new LunarAuthData()));

        // when
        AgentFailedAuthenticationResult result =
                (AgentFailedAuthenticationResult) signInToLunarStep.execute(request);

        // then
        LunarTestUtils.assertFailedResultEquals(expected, result);
    }

    private Object[] failedSignInRequestParams() {
        return new Object[] {
            new Object[] {true, new SessionExpiredError()},
            new Object[] {false, new AccessTokenFetchingFailureError()},
        };
    }

    private void setAutoAuth(boolean isAutoAuth) {
        processState.setAutoAuth(isAutoAuth);
        stateAccessor.storeState(processState);
    }

    @Test
    @Parameters(method = "emptyTokenParams")
    public void shouldReturnFailedResultWhenTokenIsEmpty(
            String token, boolean isAutoAuth, AgentBankApiError error) {
        // given
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setToken(token);
        when(apiClient.signIn(LUNAR_PASSWORD, ACCESS_TOKEN, DEVICE_ID)).thenReturn(tokenResponse);

        // and
        setAutoAuth(isAutoAuth);

        // and
        AgentFailedAuthenticationResult expected =
                new AgentFailedAuthenticationResult(
                        error, LunarTestUtils.toPersistedData(new LunarAuthData()));

        // when
        AgentFailedAuthenticationResult result =
                (AgentFailedAuthenticationResult) signInToLunarStep.execute(request);

        // then
        LunarTestUtils.assertFailedResultEquals(expected, result);
    }

    private Object[] emptyTokenParams() {
        return new Object[] {
            new Object[] {null, true, new SessionExpiredError()},
            new Object[] {null, false, new AccessTokenFetchingFailureError()},
            new Object[] {"", true, new SessionExpiredError()},
            new Object[] {"", false, new AccessTokenFetchingFailureError()},
        };
    }
}
