package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import agents_platform_framework.org.springframework.http.HttpStatus;
import agents_platform_framework.org.springframework.web.server.ResponseStatusException;
import java.nio.file.Paths;
import java.time.Clock;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.NoUserInteractionResponseError;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.LunarNemIdParametersFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.NemIdIframeAttributes;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.client.AuthenticationApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessState;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.NemIdParamsResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppCancelledError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppTimedOutError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppUnknownError;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class GetNemIdTokenStepTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/lunar/resources";

    private static final String NEM_ID_TOKEN =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><ds:SignatureValue>abcdefghij+abcdef/O002/</ds:SignatureValue>";
    private static final String B64_NEM_ID_TOKEN =
            "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiID8+PGRzOlNpZ25hdHVyZVZhbHVlPmFiY2RlZmdoaWorYWJjZGVmL08wMDIvPC9kczpTaWduYXR1cmVWYWx1ZT4=";
    private static final String CHALLENGE = "1234567890123";

    private GetNemIdTokenStep getNemIdTokenStep;
    private AuthenticationApiClient apiClient;
    private AgentProceedNextStepAuthenticationRequest request;
    private RandomValueGenerator randomValueGenerator;
    private NemIdIFrameController iFrameController;
    private NemIdParamsResponse nemIdParamsResponse;

    @Before
    public void setup() {
        randomValueGenerator = new MockRandomValueGenerator();
        LunarDataAccessorFactory dataAccessorFactory =
                new LunarDataAccessorFactory(new ObjectMapperFactory().getInstance());
        apiClient = mock(AuthenticationApiClient.class);
        NemIdIframeAttributes nemIdIframeAttributes = mock(NemIdIframeAttributes.class);
        iFrameController = mock(NemIdIFrameController.class);
        getNemIdTokenStep =
                new GetNemIdTokenStep(
                        dataAccessorFactory,
                        apiClient,
                        nemIdIframeAttributes,
                        randomValueGenerator);

        LunarProcessState processState = new LunarProcessState();
        LunarAuthData initialData = new LunarAuthData();

        LunarProcessStateAccessor stateAccessor =
                StepsUtils.getProcessStateAccessor(dataAccessorFactory, processState);
        LunarAuthDataAccessor authDataAccessor =
                StepsUtils.getAuthDataAccessor(dataAccessorFactory, initialData);

        request =
                StepsUtils.getProceedNextStepAuthRequest(
                        stateAccessor, authDataAccessor, processState, initialData);

        nemIdParamsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "nem_id_parameters.json").toFile(),
                        NemIdParamsResponse.class);

        when(nemIdIframeAttributes.getParametersFetcher())
                .thenReturn(new LunarNemIdParametersFetcher(Clock.systemDefaultZone()));
        when(nemIdIframeAttributes.getNemIdIFrameController()).thenReturn(iFrameController);
    }

    @Test
    public void shouldGetNemIdTokenAndReturnSucceededResult() {
        // given
        when(apiClient.getNemIdParameters(randomValueGenerator.getUUID().toString()))
                .thenReturn(nemIdParamsResponse);
        when(iFrameController.doLoginWith(any())).thenReturn(B64_NEM_ID_TOKEN);

        // and
        LunarAuthData expectedData = new LunarAuthData();
        expectedData.setDeviceId(randomValueGenerator.getUUID().toString());

        // and
        LunarProcessState expectedState = new LunarProcessState();
        expectedState.setNemIdToken(NEM_ID_TOKEN);
        expectedState.setChallenge(CHALLENGE);

        // when
        AgentAuthenticationResult result = getNemIdTokenStep.execute(request);

        // then
        assertThat(result)
                .isEqualTo(
                        new AgentUserInteractionDefinitionResult(
                                AgentAuthenticationProcessStep.identifier(
                                        GetLunarAccessTokenStep.class),
                                StepsUtils.getExpectedPersistedData(expectedData),
                                StepsUtils.getExpectedState(expectedState)));
    }

    @Test
    public void shouldReturnFailedResultWhenErrorOccursWhileFetchingNemIdParameters() {
        // given
        when(apiClient.getNemIdParameters(randomValueGenerator.getUUID().toString()))
                .thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        // when
        AgentFailedAuthenticationResult result =
                (AgentFailedAuthenticationResult) getNemIdTokenStep.execute(request);

        // then
        StepsUtils.assertFailedResultEquals(
                new AgentFailedAuthenticationResult(
                        new AuthorizationError(),
                        StepsUtils.getExpectedPersistedData(new LunarAuthData())),
                result);
    }

    @Test
    public void shouldThrowExceptionWhenChallengeIsAbsent() {
        // given
        when(apiClient.getNemIdParameters(randomValueGenerator.getUUID().toString()))
                .thenReturn(new NemIdParamsResponse());

        // when
        Throwable throwable = catchThrowable(() -> getNemIdTokenStep.execute(request));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Response does not contain challenge!");
    }

    @Test
    @Parameters(method = "loginWithNemIdErrors")
    public void shouldReturnFailedResultWhenExpectedExceptionDuringNemIdLoginOccurs(
            AuthenticationException e, AgentBankApiError apiError) {
        // given
        when(apiClient.getNemIdParameters(randomValueGenerator.getUUID().toString()))
                .thenReturn(nemIdParamsResponse);
        when(iFrameController.doLoginWith(any())).thenThrow(e);

        // when
        AgentFailedAuthenticationResult result =
                (AgentFailedAuthenticationResult) getNemIdTokenStep.execute(request);

        // then
        StepsUtils.assertFailedResultEquals(
                new AgentFailedAuthenticationResult(
                        apiError, StepsUtils.getExpectedPersistedData(new LunarAuthData())),
                result);
    }

    private Object[] loginWithNemIdErrors() {
        return new Object[] {
            new Object[] {
                LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(), new AuthorizationError()
            },
            new Object[] {
                LoginError.INCORRECT_CREDENTIALS.exception(), new InvalidCredentialsError()
            },
            new Object[] {LoginError.DEFAULT_MESSAGE.exception(), new ThirdPartyAppUnknownError()},
            new Object[] {LoginError.NOT_CUSTOMER.exception(), new ThirdPartyAppUnknownError()},
            new Object[] {NemIdError.REJECTED.exception(), new ThirdPartyAppCancelledError()},
            new Object[] {NemIdError.TIMEOUT.exception(), new ThirdPartyAppTimedOutError()},
            new Object[] {NemIdError.CODEAPP_NOT_REGISTERED.exception(), new AuthorizationError()},
            new Object[] {NemIdError.INTERRUPTED.exception(), new ThirdPartyAppUnknownError()},
            new Object[] {
                SupplementalInfoError.WAIT_TIMEOUT.exception(), new NoUserInteractionResponseError()
            },
        };
    }

    @Test
    public void shouldThrowExceptionWhenNotCatchedErrorDuringNemIdLoginOccurs() {
        // given
        when(apiClient.getNemIdParameters(randomValueGenerator.getUUID().toString()))
                .thenReturn(nemIdParamsResponse);
        when(iFrameController.doLoginWith(any()))
                .thenThrow(
                        new IllegalStateException(
                                "Can't instantiate iframe element with NemId form."));

        // when
        Throwable throwable = catchThrowable(() -> getNemIdTokenStep.execute(request));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Can't instantiate iframe element with NemId form.");
    }
}
