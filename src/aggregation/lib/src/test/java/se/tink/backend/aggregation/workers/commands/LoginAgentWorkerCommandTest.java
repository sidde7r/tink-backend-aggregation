package se.tink.backend.aggregation.workers.commands;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.events.LoginAgentEventProducer;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;
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
        credentials = Mockito.mock(Credentials.class);
        credentialsRequest = Mockito.mock(CredentialsRequest.class);
        Mockito.when(credentialsRequest.getCredentials()).thenReturn(credentials);
        context = Mockito.mock(AgentWorkerCommandContext.class);
        Mockito.when(context.getRequest()).thenReturn(credentialsRequest);
        state = Mockito.mock(LoginAgentWorkerCommandState.class);
        metrics = Mockito.mock(AgentWorkerCommandMetricState.class);
        Mockito.when(metrics.init(Mockito.any())).thenReturn(metrics);

        metricAction = Mockito.mock(MetricAction.class);
        metricActionLoginType = Mockito.mock(MetricAction.class);
        metricActionIsLoggedIn = Mockito.mock(MetricAction.class);
        loginAgentEventProducer = Mockito.mock(LoginAgentEventProducer.class);

        objectUnderTest =
                new LoginAgentWorkerCommand(context, state, metrics, loginAgentEventProducer);
    }

    @Test
    public void executeForNextGenerationAgentShouldLogin() throws Exception {
        // given
        NextGenerationAgent agent = Mockito.mock(NextGenerationAgent.class);
        Mockito.when(agent.login()).thenReturn(true);
        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        Mockito.verify(metricAction, Mockito.times(1)).completed();
        Mockito.verify(metricActionLoginType, Mockito.times(1)).completed();
        Assert.assertEquals(result, AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void executeForAbstractAgentShouldLogin() throws Exception {
        // given
        AbstractAgent agent = Mockito.mock(AbstractAgent.class);
        Mockito.when(agent.login()).thenReturn(true);
        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        Mockito.verify(metricAction, Mockito.times(1)).completed();
        Mockito.verify(metricActionLoginType, Mockito.times(1)).completed();
        Assert.assertEquals(result, AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void executeForNextGenerationAgentShouldAbortLogin() throws Exception {
        // given
        NextGenerationAgent agent = Mockito.mock(NextGenerationAgent.class);
        Mockito.when(agent.login()).thenReturn(false);
        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        Mockito.verify(metricAction, Mockito.times(1)).failed();
        Mockito.verify(metricActionLoginType, Mockito.times(1)).failed();
        Assert.assertEquals(result, AgentWorkerCommandResult.ABORT);
    }

    @Test
    public void executeForProgressiveAuthAgentShouldLogin() throws Exception {
        // given
        SteppableAuthenticationResponse steppableAuthenticationResponse =
                SteppableAuthenticationResponse.finalResponse();
        ProgressiveAuthAgent agent = Mockito.mock(ProgressiveAuthAgent.class);
        Mockito.when(agent.login(Mockito.any())).thenReturn(steppableAuthenticationResponse);
        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        Mockito.verify(metricAction, Mockito.times(1)).completed();
        Mockito.verify(metricActionLoginType, Mockito.times(1)).completed();
        Assert.assertEquals(result, AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void executeForBankIdExceptionShouldAbortLogin() throws Exception {
        // given
        NextGenerationAgent agent = Mockito.mock(NextGenerationAgent.class);
        Mockito.when(agent.login())
                .thenThrow(new BankIdException(BankIdError.CANCELLED, new LocalizableKey("key")));

        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        Mockito.verify(metricAction, Mockito.times(1)).cancelled();
        Mockito.verify(metricActionLoginType, Mockito.times(1)).cancelled();
        Mockito.verify(context, Mockito.times(1))
                .updateStatus(Mockito.eq(CredentialsStatus.UNCHANGED), Mockito.anyString());
        Assert.assertEquals(result, AgentWorkerCommandResult.ABORT);
    }

    @Test
    public void executeForLoginBankServiceExceptionShouldAbortLogin() throws Exception {
        // given
        NextGenerationAgent agent = Mockito.mock(NextGenerationAgent.class);
        Mockito.when(agent.login())
                .thenThrow(
                        new BankServiceException(
                                BankServiceError.BANK_SIDE_FAILURE, new LocalizableKey("key")));

        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        Mockito.verify(metricAction, Mockito.times(1)).unavailable();
        Mockito.verify(metricActionLoginType, Mockito.times(1)).unavailable();
        Mockito.verify(context, Mockito.times(1))
                .updateStatus(Mockito.eq(CredentialsStatus.TEMPORARY_ERROR), Mockito.anyString());
        Assert.assertEquals(result, AgentWorkerCommandResult.ABORT);
    }

    @Test
    public void executeForIsLoggedInBankServiceExceptionShouldAbortLogin() throws Exception {
        // given
        NextGenerationAgent agent = Mockito.mock(NextGenerationAgent.class);
        Mockito.when(agent.isLoggedIn())
                .thenThrow(
                        new BankServiceException(
                                BankServiceError.BANK_SIDE_FAILURE, new LocalizableKey("key")));

        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        Mockito.verify(metricActionIsLoggedIn, Mockito.times(1)).unavailable();
        Mockito.verify(context, Mockito.times(1))
                .updateStatus(Mockito.eq(CredentialsStatus.TEMPORARY_ERROR));
        Assert.assertEquals(result, AgentWorkerCommandResult.ABORT);
    }

    @Test
    public void executeForAuthenticationExceptionShouldAbortLogin() throws Exception {
        // given
        NextGenerationAgent agent = Mockito.mock(NextGenerationAgent.class);
        AuthenticationException exception = Mockito.mock(AuthenticationException.class);
        Mockito.when(exception.getUserMessage()).thenReturn(new LocalizableKey("key"));
        Mockito.when(agent.login()).thenThrow(exception);

        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        Mockito.verify(metricAction, Mockito.times(1)).cancelled();
        Mockito.verify(metricActionLoginType, Mockito.times(1)).cancelled();
        Mockito.verify(context, Mockito.times(1))
                .updateStatus(
                        Mockito.eq(CredentialsStatus.AUTHENTICATION_ERROR), Mockito.anyString());
        Assert.assertEquals(result, AgentWorkerCommandResult.ABORT);
    }

    @Test
    public void executeForAuthorizationExceptionShouldAbortLogin() throws Exception {
        // given
        NextGenerationAgent agent = Mockito.mock(NextGenerationAgent.class);
        AuthenticationException exception = Mockito.mock(AuthenticationException.class);
        Mockito.when(exception.getUserMessage()).thenReturn(new LocalizableKey("key"));
        Mockito.when(agent.login()).thenThrow(exception);

        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        Mockito.verify(metricAction, Mockito.times(1)).cancelled();
        Mockito.verify(metricActionLoginType, Mockito.times(1)).cancelled();
        Mockito.verify(context, Mockito.times(1))
                .updateStatus(
                        Mockito.eq(CredentialsStatus.AUTHENTICATION_ERROR), Mockito.anyString());
        Assert.assertEquals(result, AgentWorkerCommandResult.ABORT);
    }

    @Test
    public void executeForAnyExceptionShouldAbortLogin() throws Exception {
        // given
        NextGenerationAgent agent = Mockito.mock(NextGenerationAgent.class);
        Mockito.when(agent.login()).thenThrow(new IllegalArgumentException("message"));

        prepareStateForLogin(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        Mockito.verify(metricAction, Mockito.times(1)).failed();
        Mockito.verify(metricActionLoginType, Mockito.times(1)).failed();
        Mockito.verify(context, Mockito.times(1))
                .updateStatus(Mockito.eq(CredentialsStatus.TEMPORARY_ERROR));
        Assert.assertEquals(result, AgentWorkerCommandResult.ABORT);
    }

    private void prepareStateForLogin(Agent agent) {
        Mockito.when(context.getAgent()).thenReturn(agent);
        Mockito.when(
                        metrics.buildAction(
                                Mockito.eq(
                                        new MetricId.MetricLabels()
                                                .add("action", MetricName.IS_LOGGED_IN))))
                .thenReturn(metricActionIsLoggedIn);
        Mockito.when(
                        metrics.buildAction(
                                Mockito.eq(
                                        new MetricId.MetricLabels()
                                                .add("action", MetricName.LOGIN))))
                .thenReturn(metricAction);
        Mockito.when(
                        metrics.buildAction(
                                Mockito.eq(
                                        new MetricId.MetricLabels()
                                                .add("action", MetricName.LOGIN_CRON))))
                .thenReturn(metricActionLoginType);
        Catalog catalog = Mockito.mock(Catalog.class);
        Mockito.when(catalog.getString(Mockito.any(LocalizableKey.class)))
                .thenReturn("localizedString");
        Mockito.when(context.getCatalog()).thenReturn(catalog);
    }
}
