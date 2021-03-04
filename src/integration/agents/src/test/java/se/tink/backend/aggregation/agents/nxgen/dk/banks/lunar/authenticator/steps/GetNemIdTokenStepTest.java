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
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.LunarNemIdParametersFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.LunarTestUtils;
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
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppCancelledError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppNoClientError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppTimedOutError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppUnknownError;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameController;
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
                LunarTestUtils.getProcessStateAccessor(dataAccessorFactory, processState);
        LunarAuthDataAccessor authDataAccessor =
                LunarTestUtils.getAuthDataAccessor(dataAccessorFactory, initialData);

        request =
                LunarTestUtils.getProceedNextStepAuthRequest(
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
        when(iFrameController.logInWithCredentials(any())).thenReturn(B64_NEM_ID_TOKEN);

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
                        new AgentProceedNextStepAuthenticationResult(
                                AgentAuthenticationProcessStep.identifier(
                                        GetLunarAccessTokenStep.class),
                                LunarTestUtils.toProcessState(expectedState),
                                LunarTestUtils.toPersistedData(expectedData)));
    }

    @Test
    public void shouldReturnFailedResultWhenErrorOccursWhileFetchingNemIdParameters() {
        // given
        when(apiClient.getNemIdParameters(randomValueGenerator.getUUID().toString()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));

        // when
        AgentFailedAuthenticationResult result =
                (AgentFailedAuthenticationResult) getNemIdTokenStep.execute(request);

        // then
        LunarTestUtils.assertFailedResultEquals(
                new AgentFailedAuthenticationResult(
                        new AuthorizationError(),
                        LunarTestUtils.toPersistedData(new LunarAuthData())),
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
            AgentException e, AgentBankApiError apiError) {
        // given
        when(apiClient.getNemIdParameters(randomValueGenerator.getUUID().toString()))
                .thenReturn(nemIdParamsResponse);
        when(iFrameController.logInWithCredentials(any())).thenThrow(e);

        // when
        AgentFailedAuthenticationResult result =
                (AgentFailedAuthenticationResult) getNemIdTokenStep.execute(request);

        // then
        LunarTestUtils.assertFailedResultEquals(
                new AgentFailedAuthenticationResult(
                        apiError, LunarTestUtils.toPersistedData(new LunarAuthData())),
                result);
    }

    private Object[] loginWithNemIdErrors() {
        return new Object[] {
            new Object[] {
                LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(),
                new AuthorizationError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.GENERAL_AUTHORIZATION_ERROR.getCode(),
                                LoginError.CREDENTIALS_VERIFICATION_ERROR))
            },
            new Object[] {
                LoginError.INCORRECT_CREDENTIALS.exception(),
                new InvalidCredentialsError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.INVALID_CREDENTIALS.getCode(),
                                LoginError.INCORRECT_CREDENTIALS))
            },
            new Object[] {
                LoginError.NOT_CUSTOMER.exception(),
                new ThirdPartyAppNoClientError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.INVALID_CREDENTIALS.getCode(), LoginError.NOT_CUSTOMER))
            },
            new Object[] {
                LoginError.DEFAULT_MESSAGE.exception(),
                new ThirdPartyAppUnknownError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.THIRD_PARTY_APP_UNKNOWN_ERROR.getCode(),
                                LoginError.DEFAULT_MESSAGE))
            },
            new Object[] {
                NemIdError.REJECTED.exception(),
                new ThirdPartyAppCancelledError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.THIRD_PARTY_APP_CANCELLED.getCode(),
                                NemIdError.REJECTED))
            },
            new Object[] {
                NemIdError.INTERRUPTED.exception(),
                new AuthenticationError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.THIRD_PARTY_APP_UNKNOWN_ERROR.getCode(),
                                NemIdError.INTERRUPTED))
            },
            new Object[] {
                NemIdError.NEMID_LOCKED.exception(),
                new AuthenticationError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.THIRD_PARTY_APP_UNKNOWN_ERROR.getCode(),
                                NemIdError.NEMID_LOCKED))
            },
            new Object[] {
                NemIdError.NEMID_BLOCKED.exception(),
                new AuthenticationError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.THIRD_PARTY_APP_UNKNOWN_ERROR.getCode(),
                                NemIdError.NEMID_BLOCKED))
            },
            new Object[] {
                NemIdError.INVALID_CODE_CARD_CODE.exception(),
                new InvalidCredentialsError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.INVALID_CREDENTIALS.getCode(),
                                NemIdError.INVALID_CODE_CARD_CODE))
            },
            new Object[] {
                NemIdError.USE_NEW_CODE_CARD.exception(),
                new InvalidCredentialsError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.INVALID_CREDENTIALS.getCode(),
                                NemIdError.USE_NEW_CODE_CARD))
            },
            new Object[] {
                NemIdError.INVALID_CODE_TOKEN_CODE.exception(),
                new InvalidCredentialsError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.INVALID_CREDENTIALS.getCode(),
                                NemIdError.INVALID_CODE_TOKEN_CODE))
            },
            new Object[] {
                NemIdError.TIMEOUT.exception(),
                new ThirdPartyAppTimedOutError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.THIRD_PARTY_APP_TIMEOUT.getCode(), NemIdError.TIMEOUT))
            },
            new Object[] {
                NemIdError.CODE_TOKEN_NOT_SUPPORTED.exception(),
                new ThirdPartyAppUnknownError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.THIRD_PARTY_APP_UNKNOWN_ERROR.getCode(),
                                NemIdError.CODE_TOKEN_NOT_SUPPORTED))
            },
            new Object[] {
                SupplementalInfoError.WAIT_TIMEOUT.exception(), new NoUserInteractionResponseError()
            },
            new Object[] {
                SupplementalInfoError.NO_VALID_CODE.exception(),
                new NoUserInteractionResponseError()
            },
            new Object[] {
                SupplementalInfoError.UNKNOWN.exception(),
                new AuthenticationError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.GENERAL_AUTHORIZATION_ERROR.getCode(),
                                SupplementalInfoError.UNKNOWN))
            },
            new Object[] {
                BankServiceError.BANK_SIDE_FAILURE.exception(),
                new ServerError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.HTTP_RESPONSE_ERROR.getCode(),
                                BankServiceError.BANK_SIDE_FAILURE))
            }
        };
    }

    @Test
    public void shouldThrowExceptionWhenNotCaughtErrorDuringNemIdLoginOccurs() {
        // given
        when(apiClient.getNemIdParameters(randomValueGenerator.getUUID().toString()))
                .thenReturn(nemIdParamsResponse);
        when(iFrameController.logInWithCredentials(any()))
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
