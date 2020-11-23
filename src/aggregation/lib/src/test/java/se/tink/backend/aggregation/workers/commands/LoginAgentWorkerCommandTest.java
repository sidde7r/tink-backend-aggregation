package se.tink.backend.aggregation.workers.commands;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.framework.error.ServerError;
import se.tink.backend.aggregation.events.LoginAgentEventProducer;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.TinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.workers.commands.LoginAgentWorkerCommand.MetricName;
import se.tink.backend.aggregation.workers.commands.state.LoginAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.metrics.core.MetricId;

public class LoginAgentWorkerCommandTest {

    private LoginAgentWorkerCommand objectUnderTest;
    private AgentWorkerCommandContext context;
    private LoginAgentWorkerCommandState state;
    private AgentWorkerCommandMetricState metrics;
    private CredentialsRequest credentialsRequest;
    private Credentials credentials;
    private MetricAction metricAction;
    private MetricAction metricActionLoginType;
    private MetricAction metricActionIsLoggedIn;
    private LoginAgentEventProducer loginAgentEventProducer;

    @Before
    public void init() {
        credentials = mock(Credentials.class);
        Provider provider = mock(Provider.class);
        when(provider.getName()).thenReturn("a-b-c");
        credentialsRequest = mock(CredentialsRequest.class);
        when(credentialsRequest.getProvider()).thenReturn(provider);
        when(credentialsRequest.getCredentials()).thenReturn(credentials);
        context = mock(AgentWorkerCommandContext.class);
        when(context.getRequest()).thenReturn(credentialsRequest);
        state = mock(LoginAgentWorkerCommandState.class);
        metrics = mock(AgentWorkerCommandMetricState.class);
        when(metrics.init(any(), any())).thenReturn(metrics);
        when(metrics.init(any())).thenReturn(metrics);

        metricAction = mock(MetricAction.class);
        metricActionLoginType = mock(MetricAction.class);
        metricActionIsLoggedIn = mock(MetricAction.class);
        loginAgentEventProducer = mock(LoginAgentEventProducer.class);

        objectUnderTest =
                new LoginAgentWorkerCommand(context, state, metrics, loginAgentEventProducer);
    }

    @Test
    public void executeForAgentPlatformAuthenticatorAgentShouldLogin() throws Exception {
        // given
        AgentSucceededAuthenticationResult agentSucceededAuthenticationResult =
                new AgentSucceededAuthenticationResult(
                        new AgentAuthenticationPersistedData(new HashMap<>()));
        DummyTestAgentPlatformAuthenticatorAgent agent =
                createAgentPlatformAuthenticationAgent(agentSucceededAuthenticationResult);
        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        verify(metricAction, times(1)).completed();
        verify(metricActionLoginType, times(1)).completed();
        Assert.assertEquals(result, AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void executeForAgentPlatformAuthenticatorAgentShouldAbortLogin() throws Exception {
        // given
        AgentFailedAuthenticationResult agentAuthenticationResult =
                new AgentFailedAuthenticationResult(
                        new ServerError(), new AgentAuthenticationPersistedData(new HashMap<>()));
        DummyTestAgentPlatformAuthenticatorAgent agent =
                createAgentPlatformAuthenticationAgent(agentAuthenticationResult);
        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        verify(metricAction, times(1)).unavailable();
        verify(metricActionLoginType, times(1)).unavailable();
        Assert.assertEquals(result, AgentWorkerCommandResult.ABORT);
    }

    private DummyTestAgentPlatformAuthenticatorAgent createAgentPlatformAuthenticationAgent(
            AgentAuthenticationResult stepResult) {
        AgentAuthenticationProcess authenticationProcess =
                Mockito.mock(AgentAuthenticationProcess.class);
        AgentAuthenticationProcessStep firstStep =
                Mockito.mock(AgentAuthenticationProcessStep.class);
        Mockito.when(firstStep.execute(Mockito.any())).thenReturn(stepResult);
        Mockito.when(authenticationProcess.getStartStep()).thenReturn(firstStep);
        DummyTestAgentPlatformAuthenticatorAgent agent =
                Mockito.mock(DummyTestAgentPlatformAuthenticatorAgent.class);
        Mockito.when(agent.getAuthenticationProcess()).thenReturn(authenticationProcess);
        Mockito.when(agent.getPersistentStorage()).thenReturn(new PersistentStorage());
        Mockito.doCallRealMethod().when(agent).accept(Mockito.any());
        return agent;
    }

    @Test
    public void executeForNextGenerationAgentShouldLogin() throws Exception {
        // given
        NextGenerationAgent agent = mock(NextGenerationAgent.class);
        when(agent.login()).thenReturn(true);
        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        verify(metricAction, times(1)).completed();
        verify(metricActionLoginType, times(1)).completed();
        Assert.assertEquals(result, AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void executeForAbstractAgentShouldLogin() throws Exception {
        // given
        AbstractAgent agent = mock(AbstractAgent.class);
        when(agent.login()).thenReturn(true);
        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        verify(metricAction, times(1)).completed();
        verify(metricActionLoginType, times(1)).completed();
        Assert.assertEquals(result, AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void executeForNextGenerationAgentShouldAbortLogin() throws Exception {
        // given
        NextGenerationAgent agent = mock(NextGenerationAgent.class);
        when(agent.login()).thenReturn(false);
        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        verify(metricAction, times(1)).failed();
        verify(metricActionLoginType, times(1)).failed();
        Assert.assertEquals(result, AgentWorkerCommandResult.ABORT);
    }

    @Test
    public void executeForProgressiveAuthAgentShouldLogin() throws Exception {
        // given
        SteppableAuthenticationResponse steppableAuthenticationResponse =
                SteppableAuthenticationResponse.finalResponse();
        ProgressiveAuthAgent agent = mock(ProgressiveAuthAgent.class);
        when(agent.login(any())).thenReturn(steppableAuthenticationResponse);
        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        verify(metricAction, times(1)).completed();
        verify(metricActionLoginType, times(1)).completed();
        Assert.assertEquals(result, AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void executeForBankIdExceptionShouldAbortLogin() throws Exception {
        // given
        NextGenerationAgent agent = mock(NextGenerationAgent.class);
        when(agent.login())
                .thenThrow(new BankIdException(BankIdError.CANCELLED, new LocalizableKey("key")));

        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        verify(metricAction, times(1)).cancelled();
        verify(metricActionLoginType, times(1)).cancelled();
        verify(context, times(1)).updateStatus(eq(CredentialsStatus.UNCHANGED), anyString());
        Assert.assertEquals(result, AgentWorkerCommandResult.ABORT);
    }

    @Test
    public void executeForLoginBankServiceExceptionShouldAbortLogin() throws Exception {
        // given
        NextGenerationAgent agent = mock(NextGenerationAgent.class);
        when(agent.login())
                .thenThrow(
                        new BankServiceException(
                                BankServiceError.BANK_SIDE_FAILURE, new LocalizableKey("key")));

        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        verify(metricAction, times(1)).unavailable();
        verify(metricActionLoginType, times(1)).unavailable();
        verify(context, times(1)).updateStatus(eq(CredentialsStatus.TEMPORARY_ERROR), anyString());
        Assert.assertEquals(result, AgentWorkerCommandResult.ABORT);
    }

    @Test
    public void executeForIsLoggedInBankServiceExceptionShouldAbortLogin() throws Exception {
        // given
        NextGenerationAgent agent = mock(NextGenerationAgent.class);
        when(agent.isLoggedIn())
                .thenThrow(
                        new BankServiceException(
                                BankServiceError.BANK_SIDE_FAILURE, new LocalizableKey("key")));

        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        verify(metricActionIsLoggedIn, times(1)).unavailable();
        verify(context, times(1)).updateStatus(eq(CredentialsStatus.TEMPORARY_ERROR));
        Assert.assertEquals(result, AgentWorkerCommandResult.ABORT);
    }

    @Test
    public void executeForAuthenticationExceptionShouldAbortLogin() throws Exception {
        // given
        NextGenerationAgent agent = mock(NextGenerationAgent.class);
        AuthenticationException exception = mock(AuthenticationException.class);
        when(exception.getUserMessage()).thenReturn(new LocalizableKey("key"));
        when(agent.login()).thenThrow(exception);

        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        verify(metricAction, times(1)).cancelled();
        verify(metricActionLoginType, times(1)).cancelled();
        verify(context, times(1))
                .updateStatus(eq(CredentialsStatus.AUTHENTICATION_ERROR), anyString());
        Assert.assertEquals(result, AgentWorkerCommandResult.ABORT);
    }

    @Test
    public void executeForAuthorizationExceptionShouldAbortLogin() throws Exception {
        // given
        NextGenerationAgent agent = mock(NextGenerationAgent.class);
        AuthenticationException exception = mock(AuthenticationException.class);
        when(exception.getUserMessage()).thenReturn(new LocalizableKey("key"));
        when(agent.login()).thenThrow(exception);

        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        verify(metricAction, times(1)).cancelled();
        verify(metricActionLoginType, times(1)).cancelled();
        verify(context, times(1))
                .updateStatus(eq(CredentialsStatus.AUTHENTICATION_ERROR), anyString());
        Assert.assertEquals(result, AgentWorkerCommandResult.ABORT);
    }

    @Test
    public void executeForAnyExceptionShouldAbortLogin() throws Exception {
        // given
        NextGenerationAgent agent = mock(NextGenerationAgent.class);
        when(agent.login()).thenThrow(new IllegalArgumentException("message"));

        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        verify(metricAction, times(1)).failed();
        verify(metricActionLoginType, times(1)).failed();
        verify(context, times(1)).updateStatus(eq(CredentialsStatus.TEMPORARY_ERROR));
        Assert.assertEquals(result, AgentWorkerCommandResult.ABORT);
    }

    private void prepareStateForLogin(Agent agent) {
        when(context.getAgent()).thenReturn(agent);
        when(metrics.buildAction(
                        eq(new MetricId.MetricLabels().add("action", MetricName.IS_LOGGED_IN))))
                .thenReturn(metricActionIsLoggedIn);
        when(metrics.buildAction(eq(new MetricId.MetricLabels().add("action", MetricName.LOGIN))))
                .thenReturn(metricAction);
        when(metrics.buildAction(
                        eq(new MetricId.MetricLabels().add("action", MetricName.LOGIN_CRON))))
                .thenReturn(metricActionLoginType);
        Catalog catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("localizedString");
        when(context.getCatalog()).thenReturn(catalog);
    }

    private AgentComponentProvider createDummyAgentComponentProvider() {
        return new AgentComponentProvider(
                Mockito.mock(TinkHttpClientProvider.class),
                Mockito.mock(SupplementalInformationProvider.class),
                Mockito.mock(AgentContextProvider.class),
                Mockito.mock(GeneratedValueProvider.class));
    }
}
