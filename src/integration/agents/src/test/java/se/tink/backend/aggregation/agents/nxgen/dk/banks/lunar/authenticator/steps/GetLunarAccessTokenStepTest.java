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
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.AccessTokenResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccessTokenFetchingFailureError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppNoClientError;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class GetLunarAccessTokenStepTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/lunar/resources";

    private static final String DEVICE_ID = "some test id";
    private static final String ACCESS_TOKEN = "this_is_test_access_token";
    private static final String CHALLENGE = "1234567890123";
    private static final String NEM_ID_TOKEN =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<ds:SignatureValue>\nabcdefghij+abcdef/O002/</ds:SignatureValue>";

    private GetLunarAccessTokenStep getLunarAccessTokenStep;
    private AuthenticationApiClient apiClient;
    private AgentProceedNextStepAuthenticationRequest request;

    @Before
    public void setup() {
        LunarDataAccessorFactory dataAccessorFactory =
                new LunarDataAccessorFactory(new ObjectMapperFactory().getInstance());
        apiClient = mock(AuthenticationApiClient.class);
        getLunarAccessTokenStep = new GetLunarAccessTokenStep(dataAccessorFactory, apiClient);

        LunarProcessState processState = new LunarProcessState();
        processState.setNemIdToken(NEM_ID_TOKEN);
        processState.setChallenge(CHALLENGE);

        LunarAuthData initialData = new LunarAuthData();
        initialData.setDeviceId(DEVICE_ID);

        LunarProcessStateAccessor stateAccessor =
                LunarTestUtils.getProcessStateAccessor(dataAccessorFactory, processState);
        LunarAuthDataAccessor authDataAccessor =
                LunarTestUtils.getAuthDataAccessor(dataAccessorFactory, initialData);

        request =
                LunarTestUtils.getProceedNextStepAuthRequest(
                        stateAccessor, authDataAccessor, processState, initialData);
    }

    @Test
    public void shouldPostNemIdTokenAndReturnSucceededResult() {
        // given
        AccessTokenResponse accessTokenResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "access_token_response.json").toFile(),
                        AccessTokenResponse.class);
        when(apiClient.postNemIdToken(NEM_ID_TOKEN, CHALLENGE, DEVICE_ID))
                .thenReturn(accessTokenResponse);

        // and
        LunarAuthData expectedData = new LunarAuthData();
        expectedData.setDeviceId(DEVICE_ID);
        expectedData.setAccessToken(ACCESS_TOKEN);

        // and
        LunarProcessState expectedState = new LunarProcessState();
        expectedState.setNemIdToken(NEM_ID_TOKEN);
        expectedState.setChallenge(CHALLENGE);
        expectedState.setAutoAuth(false);

        // when
        AgentAuthenticationResult result = getLunarAccessTokenStep.execute(request);

        // then
        assertThat(result)
                .isEqualTo(
                        new AgentProceedNextStepAuthenticationResult(
                                AgentAuthenticationProcessStep.identifier(SignInToLunarStep.class),
                                LunarTestUtils.toProcessState(expectedState),
                                LunarTestUtils.toPersistedData(expectedData)));
    }

    @Test
    @Parameters(method = "accessTokenParams")
    public void shouldFailWhenAccessTokenIsEmpty(String accessToken) {
        // given
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setAccessToken(accessToken);

        when(apiClient.postNemIdToken(NEM_ID_TOKEN, CHALLENGE, DEVICE_ID))
                .thenReturn(accessTokenResponse);

        // when
        AgentFailedAuthenticationResult result =
                (AgentFailedAuthenticationResult) getLunarAccessTokenStep.execute(request);

        // then
        LunarTestUtils.assertFailedResultEquals(
                new AgentFailedAuthenticationResult(
                        new AccessTokenFetchingFailureError(),
                        LunarTestUtils.toPersistedData(new LunarAuthData())),
                result);
    }

    private Object[] accessTokenParams() {
        return new Object[] {
            new Object[] {null}, new Object[] {""},
        };
    }

    @Test
    public void shouldReturnFailedResultWhenResponseStatusExceptionOccurs() {
        // given
        when(apiClient.postNemIdToken(NEM_ID_TOKEN, CHALLENGE, DEVICE_ID))
                .thenThrow(
                        new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "{\"reasonCode\": \"USER_NOT_FOUND\"}"));

        // when
        AgentFailedAuthenticationResult result =
                (AgentFailedAuthenticationResult) getLunarAccessTokenStep.execute(request);

        // then
        LunarTestUtils.assertFailedResultEquals(
                new AgentFailedAuthenticationResult(
                        new ThirdPartyAppNoClientError(),
                        LunarTestUtils.toPersistedData(new LunarAuthData())),
                result);
    }
}
