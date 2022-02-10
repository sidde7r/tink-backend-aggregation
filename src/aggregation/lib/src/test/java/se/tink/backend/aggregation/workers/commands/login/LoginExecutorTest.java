package se.tink.backend.aggregation.workers.commands.login;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAgent;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticationExecutor;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessError;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessException;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.connectivity.errors.ConnectivityErrorType;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;
import se.tink.libraries.metrics.registry.MetricRegistry;
import src.libraries.interaction_counter.InteractionCounter;

@RunWith(MockitoJUnitRunner.class)
public class LoginExecutorTest {

    // <editor-fold desc="test init setup">

    private static final String DUMMY_LOCALIZED_ERROR_MESSAGE = "localized error message";

    @Mock private AgentPlatformAgent agentPlatformAgent;

    @Mock private SubsequentProgressiveGenerationAgent subsequentProgressiveGenerationAgent;

    @Mock private Agent legacyAgent;

    @Mock private AgentPlatformAuthenticationExecutor agentPlatformAuthenticationExecutor;

    @Mock private AgentWorkerCommandContext agentWorkerCommandContext;

    @Mock private CredentialsRequest credentialsRequest;

    @Mock private Credentials credentials;

    @Mock private Catalog catalog;

    @Mock private SupplementalInformationController supplementalInformationController;

    @Mock private InteractionCounter interactionCounter;

    @Mock private DataStudioLoginEventPublisherService dataStudioLoginEventPublisherService;

    @Mock private MetricsFactory metricsFactory;

    @Mock private MetricActionIface loginMetricAction;

    @Mock private StatusUpdater statusUpdater;

    private LoginExecutor objectUnderTest;

    @Before
    public void init() {
        Mockito.when(agentWorkerCommandContext.getRequest()).thenReturn(credentialsRequest);
        Mockito.when(agentWorkerCommandContext.getCatalog()).thenReturn(catalog);
        Mockito.when(agentWorkerCommandContext.getSupplementalInteractionCounter())
                .thenReturn(interactionCounter);
        Mockito.when(agentWorkerCommandContext.getMetricRegistry())
                .thenReturn(new MetricRegistry());

        Mockito.when(metricsFactory.createLoginMetric(credentialsRequest, interactionCounter))
                .thenReturn(loginMetricAction);

        Mockito.when(credentialsRequest.getCredentials()).thenReturn(credentials);

        objectUnderTest =
                new LoginExecutor(
                        metricsFactory, statusUpdater, agentPlatformAuthenticationExecutor);
    }
    // </editor-fold>

    // <editor-fold  desc="AgentPlatformAgent tests">
    @Test
    public void agentPlatformAgentSuccessAuthenticationTest() {
        // given
        Mockito.when(agentWorkerCommandContext.getAgent()).thenReturn(agentPlatformAgent);
        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);
        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        Mockito.verify(loginMetricAction).completed();
        Mockito.verify(dataStudioLoginEventPublisherService).publishLoginSuccessEvent();
    }

    @Test
    public void agentPlatformAgentAuthenticationFailedBecauseOfAuthenticationErrorTest() {
        // given
        Mockito.when(agentWorkerCommandContext.getAgent()).thenReturn(agentPlatformAgent);
        AgentPlatformAuthenticationProcessException agentPlatformAuthenticationProcessException =
                new AgentPlatformAuthenticationProcessException(
                        new AgentPlatformAuthenticationProcessError(new InvalidCredentialsError()),
                        "exception message");
        Mockito.doThrow(agentPlatformAuthenticationProcessException)
                .when(agentPlatformAuthenticationExecutor)
                .processAuthentication(
                        agentPlatformAgent, credentialsRequest, supplementalInformationController);
        Mockito.when(
                        catalog.getString(
                                agentPlatformAuthenticationProcessException.getUserMessage()))
                .thenReturn(DUMMY_LOCALIZED_ERROR_MESSAGE);
        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);
        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).cancelled();
        Mockito.verify(dataStudioLoginEventPublisherService)
                .publishLoginResultEvent(
                        AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                .LOGIN_ERROR_INCORRECT_CREDENTIALS);
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.AUTHENTICATION_ERROR),
                        eq(DUMMY_LOCALIZED_ERROR_MESSAGE),
                        any(ConnectivityError.class));
    }

    @Test
    public void agentPlatformAgentAuthenticationFailedBecauseOfAuthorizationErrorTest() {
        // given
        Mockito.when(agentWorkerCommandContext.getAgent()).thenReturn(agentPlatformAgent);
        AgentPlatformAuthenticationProcessException agentPlatformAuthenticationProcessException =
                new AgentPlatformAuthenticationProcessException(
                        new AgentPlatformAuthenticationProcessError(new AuthorizationError()),
                        "exception message");
        Mockito.doThrow(agentPlatformAuthenticationProcessException)
                .when(agentPlatformAuthenticationExecutor)
                .processAuthentication(
                        agentPlatformAgent, credentialsRequest, supplementalInformationController);
        Mockito.when(
                        catalog.getString(
                                agentPlatformAuthenticationProcessException.getUserMessage()))
                .thenReturn(DUMMY_LOCALIZED_ERROR_MESSAGE);
        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);
        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).cancelled();
        Mockito.verify(dataStudioLoginEventPublisherService)
                .publishLoginResultEvent(
                        AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                .AUTHORIZATION_ERROR_UNKNOWN);
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.AUTHENTICATION_ERROR),
                        eq(DUMMY_LOCALIZED_ERROR_MESSAGE),
                        any(ConnectivityError.class));
    }

    @Test
    public void agentPlatformAgentAuthenticationFailedBecauseOfServerErrorTest() {
        // given
        Mockito.when(agentWorkerCommandContext.getAgent()).thenReturn(agentPlatformAgent);
        AgentPlatformAuthenticationProcessException agentPlatformAuthenticationProcessException =
                new AgentPlatformAuthenticationProcessException(
                        new AgentPlatformAuthenticationProcessError(new ServerError()),
                        "exception message");
        Mockito.doThrow(agentPlatformAuthenticationProcessException)
                .when(agentPlatformAuthenticationExecutor)
                .processAuthentication(
                        agentPlatformAgent, credentialsRequest, supplementalInformationController);
        Mockito.when(
                        catalog.getString(
                                agentPlatformAuthenticationProcessException.getUserMessage()))
                .thenReturn(DUMMY_LOCALIZED_ERROR_MESSAGE);
        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);
        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).unavailable();
        Mockito.verify(dataStudioLoginEventPublisherService)
                .publishLoginResultEvent(
                        AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                .BANK_SERVICE_ERROR_UNKNOWN);
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.TEMPORARY_ERROR),
                        eq(DUMMY_LOCALIZED_ERROR_MESSAGE),
                        any(ConnectivityError.class));
    }

    @Test
    public void agentPlatformAgentAuthenticationFailedBecauseOfRuntimeExceptionTest() {
        // given
        Mockito.when(agentWorkerCommandContext.getAgent()).thenReturn(agentPlatformAgent);
        Mockito.doThrow(new IllegalStateException())
                .when(agentPlatformAuthenticationExecutor)
                .processAuthentication(
                        agentPlatformAgent, credentialsRequest, supplementalInformationController);
        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);
        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).failed();
        Mockito.verify(dataStudioLoginEventPublisherService).publishLoginErrorUnknown();
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.TEMPORARY_ERROR),
                        eq(null),
                        any(ConnectivityError.class));
    }
    // </editor-fold>

    // <editor-fold desc="SubsequentProgressiveGenerationAgent tests">

    @Test
    public void subsequenceProgressiveGenerationAgentSuccessAuthenticationTest() throws Exception {
        // given
        Mockito.when(agentWorkerCommandContext.getAgent()).thenReturn(legacyAgent);
        Mockito.when(legacyAgent.login()).thenReturn(true);
        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);
        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        Mockito.verify(loginMetricAction).completed();
        Mockito.verify(dataStudioLoginEventPublisherService).publishLoginSuccessEvent();
    }

    @Test
    public void subsequenceProgressiveGenerationAgentAuthenticationExceptionTest()
            throws Exception {
        // given
        Mockito.when(agentWorkerCommandContext.getAgent())
                .thenReturn(subsequentProgressiveGenerationAgent);
        AuthenticationException authenticationException =
                LoginError.INCORRECT_CREDENTIALS.exception();
        Mockito.when(
                        subsequentProgressiveGenerationAgent.login(
                                SteppableAuthenticationRequest.initialRequest(credentials)))
                .thenThrow(authenticationException);
        Mockito.when(catalog.getString(authenticationException.getUserMessage()))
                .thenReturn(DUMMY_LOCALIZED_ERROR_MESSAGE);
        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);
        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).cancelled();
        Mockito.verify(dataStudioLoginEventPublisherService)
                .publishLoginAuthenticationErrorEvent(authenticationException);
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.AUTHENTICATION_ERROR),
                        eq(DUMMY_LOCALIZED_ERROR_MESSAGE),
                        any(ConnectivityError.class));
    }

    @Test
    public void subsequenceProgressiveGenerationAgentAuthorizationExceptionTest() throws Exception {
        // given
        Mockito.when(agentWorkerCommandContext.getAgent())
                .thenReturn(subsequentProgressiveGenerationAgent);
        AuthorizationException authorizationException =
                se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError
                        .ACCOUNT_BLOCKED
                        .exception();
        Mockito.when(
                        subsequentProgressiveGenerationAgent.login(
                                SteppableAuthenticationRequest.initialRequest(credentials)))
                .thenThrow(authorizationException);
        Mockito.when(catalog.getString(authorizationException.getUserMessage()))
                .thenReturn(DUMMY_LOCALIZED_ERROR_MESSAGE);
        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);
        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).cancelled();
        Mockito.verify(dataStudioLoginEventPublisherService)
                .publishLoginAuthorizationErrorEvent(authorizationException);
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.AUTHENTICATION_ERROR),
                        eq(DUMMY_LOCALIZED_ERROR_MESSAGE),
                        any(ConnectivityError.class));
    }

    @Test
    public void subsequenceProgressiveGenerationAgentBankServiceExceptionTest() throws Exception {
        // given
        Mockito.when(agentWorkerCommandContext.getAgent())
                .thenReturn(subsequentProgressiveGenerationAgent);
        BankServiceException bankServiceException = BankServiceError.NO_BANK_SERVICE.exception();
        Mockito.when(
                        subsequentProgressiveGenerationAgent.login(
                                SteppableAuthenticationRequest.initialRequest(credentials)))
                .thenThrow(bankServiceException);
        Mockito.when(catalog.getString(bankServiceException.getUserMessage()))
                .thenReturn(DUMMY_LOCALIZED_ERROR_MESSAGE);
        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);
        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).unavailable();
        Mockito.verify(dataStudioLoginEventPublisherService)
                .publishLoginBankServiceErrorEvent(bankServiceException);
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.TEMPORARY_ERROR),
                        eq(DUMMY_LOCALIZED_ERROR_MESSAGE),
                        any(ConnectivityError.class));
    }

    @Test
    public void subsequenceProgressiveGenerationAgentBankIdExceptionTest() throws Exception {
        // given
        Mockito.when(agentWorkerCommandContext.getAgent())
                .thenReturn(subsequentProgressiveGenerationAgent);
        BankIdException bankIdException = BankIdError.ALREADY_IN_PROGRESS.exception();
        Mockito.when(
                        subsequentProgressiveGenerationAgent.login(
                                SteppableAuthenticationRequest.initialRequest(credentials)))
                .thenThrow(bankIdException);
        Mockito.when(catalog.getString(bankIdException.getUserMessage()))
                .thenReturn(DUMMY_LOCALIZED_ERROR_MESSAGE);
        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);
        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).cancelled();
        Mockito.verify(dataStudioLoginEventPublisherService)
                .publishLoginBankIdErrorEvent(bankIdException);
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.AUTHENTICATION_ERROR),
                        eq(DUMMY_LOCALIZED_ERROR_MESSAGE),
                        any(ConnectivityError.class));
    }

    @Test
    public void subsequenceProgressiveGenerationAgentRuntimeExceptionTest() throws Exception {
        // given
        Mockito.when(agentWorkerCommandContext.getAgent())
                .thenReturn(subsequentProgressiveGenerationAgent);
        Mockito.when(
                        subsequentProgressiveGenerationAgent.login(
                                SteppableAuthenticationRequest.initialRequest(credentials)))
                .thenThrow(new IllegalStateException());
        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);
        // then
        ArgumentCaptor<ConnectivityError> captor = ArgumentCaptor.forClass(ConnectivityError.class);
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).failed();
        Mockito.verify(dataStudioLoginEventPublisherService).publishLoginErrorUnknown();
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.TEMPORARY_ERROR), eq(null), captor.capture());
        Assertions.assertThat(captor.getValue().getType())
                .isEqualTo(ConnectivityErrorType.TINK_SIDE_ERROR);
        Assertions.assertThat(captor.getValue().getDetails().getReason())
                .isEqualTo(
                        ConnectivityErrorDetails.TinkSideErrors.TINK_INTERNAL_SERVER_ERROR.name());
    }

    // </editor-fold>

    // <editor-fold desc="SubsequentProgressiveGenerationAgent tests">

    @Test
    public void legacyAgentSuccessAuthenticationTest() throws Exception {
        // given
        Mockito.when(agentWorkerCommandContext.getAgent())
                .thenReturn(subsequentProgressiveGenerationAgent);
        Mockito.when(
                        subsequentProgressiveGenerationAgent.login(
                                SteppableAuthenticationRequest.initialRequest(credentials)))
                .thenReturn(SteppableAuthenticationResponse.finalResponse());
        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);
        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        Mockito.verify(loginMetricAction).completed();
        Mockito.verify(dataStudioLoginEventPublisherService).publishLoginSuccessEvent();
    }

    @Test
    public void legacyAgentAuthenticationExceptionTest() throws Exception {
        // given
        AuthenticationException authenticationException =
                LoginError.INCORRECT_CREDENTIALS.exception();
        Mockito.when(agentWorkerCommandContext.getAgent()).thenReturn(legacyAgent);
        Mockito.when(legacyAgent.login()).thenThrow(authenticationException);
        Mockito.when(catalog.getString(authenticationException.getUserMessage()))
                .thenReturn(DUMMY_LOCALIZED_ERROR_MESSAGE);
        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);
        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).cancelled();
        Mockito.verify(dataStudioLoginEventPublisherService)
                .publishLoginAuthenticationErrorEvent(authenticationException);
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.AUTHENTICATION_ERROR),
                        eq(DUMMY_LOCALIZED_ERROR_MESSAGE),
                        any(ConnectivityError.class));
    }

    @Test
    public void legacyAgentAuthorizationExceptionTest() throws Exception {
        // given
        AuthorizationException authorizationException =
                se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError
                        .ACCOUNT_BLOCKED
                        .exception();
        Mockito.when(agentWorkerCommandContext.getAgent()).thenReturn(legacyAgent);
        Mockito.when(legacyAgent.login()).thenThrow(authorizationException);
        Mockito.when(catalog.getString(authorizationException.getUserMessage()))
                .thenReturn(DUMMY_LOCALIZED_ERROR_MESSAGE);
        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);
        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).cancelled();
        Mockito.verify(dataStudioLoginEventPublisherService)
                .publishLoginAuthorizationErrorEvent(authorizationException);
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.AUTHENTICATION_ERROR),
                        eq(DUMMY_LOCALIZED_ERROR_MESSAGE),
                        any(ConnectivityError.class));
    }

    @Test
    public void legacyAgentBankServiceExceptionTest() throws Exception {
        // given
        BankServiceException bankServiceException = BankServiceError.NO_BANK_SERVICE.exception();
        Mockito.when(agentWorkerCommandContext.getAgent()).thenReturn(legacyAgent);
        Mockito.when(legacyAgent.login()).thenThrow(bankServiceException);
        Mockito.when(catalog.getString(bankServiceException.getUserMessage()))
                .thenReturn(DUMMY_LOCALIZED_ERROR_MESSAGE);
        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);
        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).unavailable();
        Mockito.verify(dataStudioLoginEventPublisherService)
                .publishLoginBankServiceErrorEvent(bankServiceException);
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.TEMPORARY_ERROR),
                        eq(DUMMY_LOCALIZED_ERROR_MESSAGE),
                        any(ConnectivityError.class));
    }

    @Test
    public void legacyAgentBankIdExceptionTest() throws Exception {
        // given
        BankIdException bankIdException = BankIdError.ALREADY_IN_PROGRESS.exception();
        Mockito.when(agentWorkerCommandContext.getAgent()).thenReturn(legacyAgent);
        Mockito.when(legacyAgent.login()).thenThrow(bankIdException);
        Mockito.when(catalog.getString(bankIdException.getUserMessage()))
                .thenReturn(DUMMY_LOCALIZED_ERROR_MESSAGE);
        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);
        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).cancelled();
        Mockito.verify(dataStudioLoginEventPublisherService)
                .publishLoginBankIdErrorEvent(bankIdException);
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.AUTHENTICATION_ERROR),
                        eq(DUMMY_LOCALIZED_ERROR_MESSAGE),
                        any(ConnectivityError.class));
    }

    @Test
    public void legacyAgentRuntimeExceptionTest() throws Exception {
        // given
        Mockito.when(agentWorkerCommandContext.getAgent()).thenReturn(legacyAgent);
        Mockito.when(legacyAgent.login()).thenThrow(new IllegalStateException());
        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);
        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).failed();
        Mockito.verify(dataStudioLoginEventPublisherService).publishLoginErrorUnknown();
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.TEMPORARY_ERROR),
                        eq(null),
                        any(ConnectivityError.class));
    }

    // </editor-fold>

    // <editor-fold desc="ConnectivityException tests">

    @Test
    public void connectivityUserLoginErrorTest() throws Exception {
        // given
        Mockito.when(agentWorkerCommandContext.getAgent())
                .thenReturn(subsequentProgressiveGenerationAgent);
        ConnectivityException connectivityException =
                new ConnectivityException(
                                ConnectivityErrorDetails.UserLoginErrors
                                        .DYNAMIC_CREDENTIALS_INCORRECT)
                        .withUserMessage(new LocalizableKey(DUMMY_LOCALIZED_ERROR_MESSAGE));
        Mockito.when(
                        subsequentProgressiveGenerationAgent.login(
                                SteppableAuthenticationRequest.initialRequest(credentials)))
                .thenThrow(connectivityException);
        Mockito.when(catalog.getString(connectivityException.getUserMessage()))
                .thenReturn(DUMMY_LOCALIZED_ERROR_MESSAGE);

        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);

        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).cancelled();
        Mockito.verify(dataStudioLoginEventPublisherService)
                .publishEventForConnectivityException(connectivityException);
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.AUTHENTICATION_ERROR),
                        eq(DUMMY_LOCALIZED_ERROR_MESSAGE),
                        eq(connectivityException.getError()));
    }

    @Test
    public void connectivityUserLoginErrorDynamicCredentialTimeoutTest() throws Exception {
        // given
        Mockito.when(agentWorkerCommandContext.getAgent())
                .thenReturn(subsequentProgressiveGenerationAgent);
        ConnectivityException connectivityException =
                new ConnectivityException(
                                ConnectivityErrorDetails.UserLoginErrors
                                        .DYNAMIC_CREDENTIALS_FLOW_TIMEOUT)
                        .withUserMessage(new LocalizableKey(DUMMY_LOCALIZED_ERROR_MESSAGE));
        Mockito.when(
                        subsequentProgressiveGenerationAgent.login(
                                SteppableAuthenticationRequest.initialRequest(credentials)))
                .thenThrow(connectivityException);
        Mockito.when(catalog.getString(connectivityException.getUserMessage()))
                .thenReturn(DUMMY_LOCALIZED_ERROR_MESSAGE);
        Mockito.when(credentials.getStatus())
                .thenReturn(CredentialsStatus.AWAITING_THIRD_PARTY_APP_AUTHENTICATION);

        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);

        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).cancelledDueToThirdPartyAppTimeout();
        Mockito.verify(dataStudioLoginEventPublisherService)
                .publishEventForConnectivityException(connectivityException);
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.AUTHENTICATION_ERROR),
                        eq(DUMMY_LOCALIZED_ERROR_MESSAGE),
                        eq(connectivityException.getError()));
    }

    @Test
    public void connectivityTinkSideErrorTest() throws Exception {
        // given
        Mockito.when(agentWorkerCommandContext.getAgent())
                .thenReturn(subsequentProgressiveGenerationAgent);
        ConnectivityException connectivityException =
                new ConnectivityException(
                                ConnectivityErrorDetails.TinkSideErrors.TINK_INTERNAL_SERVER_ERROR)
                        .withUserMessage(new LocalizableKey(DUMMY_LOCALIZED_ERROR_MESSAGE));
        Mockito.when(
                        subsequentProgressiveGenerationAgent.login(
                                SteppableAuthenticationRequest.initialRequest(credentials)))
                .thenThrow(connectivityException);
        Mockito.when(catalog.getString(connectivityException.getUserMessage()))
                .thenReturn(DUMMY_LOCALIZED_ERROR_MESSAGE);

        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);

        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).failed();
        Mockito.verify(dataStudioLoginEventPublisherService)
                .publishEventForConnectivityException(connectivityException);
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.TEMPORARY_ERROR),
                        eq(DUMMY_LOCALIZED_ERROR_MESSAGE),
                        eq(connectivityException.getError()));
    }

    @Test
    public void connectivityProviderErrorTest() throws Exception {
        // given
        Mockito.when(agentWorkerCommandContext.getAgent())
                .thenReturn(subsequentProgressiveGenerationAgent);
        ConnectivityException connectivityException =
                new ConnectivityException(
                                ConnectivityErrorDetails.ProviderErrors.PROVIDER_UNAVAILABLE)
                        .withUserMessage(new LocalizableKey(DUMMY_LOCALIZED_ERROR_MESSAGE));
        Mockito.when(
                        subsequentProgressiveGenerationAgent.login(
                                SteppableAuthenticationRequest.initialRequest(credentials)))
                .thenThrow(connectivityException);
        Mockito.when(catalog.getString(connectivityException.getUserMessage()))
                .thenReturn(DUMMY_LOCALIZED_ERROR_MESSAGE);

        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);

        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).unavailable();
        Mockito.verify(dataStudioLoginEventPublisherService)
                .publishEventForConnectivityException(connectivityException);
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.TEMPORARY_ERROR),
                        eq(DUMMY_LOCALIZED_ERROR_MESSAGE),
                        eq(connectivityException.getError()));
    }

    @Test
    public void connectivityAuthorizationErrorTest() throws Exception {
        // given
        Mockito.when(agentWorkerCommandContext.getAgent())
                .thenReturn(subsequentProgressiveGenerationAgent);
        ConnectivityException connectivityException =
                new ConnectivityException(
                                ConnectivityErrorDetails.AuthorizationErrors.SESSION_EXPIRED)
                        .withUserMessage(new LocalizableKey(DUMMY_LOCALIZED_ERROR_MESSAGE));
        Mockito.when(
                        subsequentProgressiveGenerationAgent.login(
                                SteppableAuthenticationRequest.initialRequest(credentials)))
                .thenThrow(connectivityException);
        Mockito.when(catalog.getString(connectivityException.getUserMessage()))
                .thenReturn(DUMMY_LOCALIZED_ERROR_MESSAGE);

        // when
        AgentWorkerCommandResult result =
                objectUnderTest.execute(
                        agentWorkerCommandContext,
                        supplementalInformationController,
                        dataStudioLoginEventPublisherService);

        // then
        Assertions.assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(loginMetricAction).cancelled();
        Mockito.verify(dataStudioLoginEventPublisherService)
                .publishEventForConnectivityException(connectivityException);
        Mockito.verify(statusUpdater)
                .updateStatusWithError(
                        eq(CredentialsStatus.AUTHENTICATION_ERROR),
                        eq(DUMMY_LOCALIZED_ERROR_MESSAGE),
                        eq(connectivityException.getError()));
    }

    // </editor-fold>

}
