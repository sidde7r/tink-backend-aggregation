package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public class ClearSensitivePayloadOnForceAuthenticateCommandTest {

    private ClearSensitivePayloadOnForceAuthenticateCommand command;
    private AgentWorkerCommandContext context;

    @Before
    public void setUp() {
        context = mock(AgentWorkerCommandContext.class, Answers.RETURNS_DEEP_STUBS);
        command = new ClearSensitivePayloadOnForceAuthenticateCommand(context);

        Agent agent = mock(MyAgent.class);
        given(context.getAgent()).willReturn(agent);
    }

    @Test
    public void
            doExecuteShouldNukeSensitiveStorageAndContinueWhenAgentIsPersistedLoginTypeAndForceAuth()
                    throws Exception {
        // given
        given(context.getRequest().shouldManualAuthBeForced()).willReturn(true);

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        verify(context.getRequest().getCredentials()).setSensitivePayloadSerialized(null);
    }

    @Test
    public void doExecuteShouldNotNukeSensitiveStorageWhenAgentIsNotPersistedLoginType()
            throws Exception {
        // given
        given(context.getAgent()).willReturn(mock(Agent.class));
        given(context.getRequest().shouldManualAuthBeForced()).willReturn(true);

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        verifyZeroInteractions(context.getRequest().getCredentials());
    }

    @Test
    public void doExecuteShouldNotNukeSensitiveStorageWhenAuthIsNotForced() throws Exception {
        // given
        given(context.getRequest().shouldManualAuthBeForced()).willReturn(false);

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        verifyZeroInteractions(context.getRequest().getCredentials());
    }

    @Test
    public void doExecuteShouldContinueDespiteExceptions() throws Exception {
        // given
        given(context.getAgent()).willThrow(RuntimeException.class);

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
    }

    abstract static class MyAgent implements Agent, PersistentLogin {}
}
