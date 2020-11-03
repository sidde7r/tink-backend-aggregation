package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public class KeepAliveAgentWorkerCommandTest {

    private KeepAliveAgentWorkerCommand command;
    private AgentWorkerCommandContext context;

    @Before
    public void setUp() {
        context = mock(AgentWorkerCommandContext.class, Answers.RETURNS_DEEP_STUBS);
        command = new KeepAliveAgentWorkerCommand(context);
    }

    @Test
    public void doExecuteShouldContinueIfAgentIsPersistedLoginType() throws Exception {
        // given
        MyPersistedAgent agent = mock(MyPersistedAgent.class, Answers.RETURNS_DEEP_STUBS);
        given(context.getAgent()).willReturn(agent);
        given(agent.keepAlive()).willReturn(true);

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        verify(agent).loadLoginSession();
    }

    @Test
    public void doExecuteShouldContinueAndClearLoginSessionIfKeepAliveIsNotSet() throws Exception {
        // given
        MyPersistedAgent agent = mock(MyPersistedAgent.class, Answers.RETURNS_DEEP_STUBS);
        given(context.getAgent()).willReturn(agent);
        given(agent.keepAlive()).willReturn(false);

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        verify(agent).loadLoginSession();
        verify(agent).clearLoginSession();
    }

    @Test
    public void doExecuteShouldAbortWhenAgentIsNotPersistedLoginType() throws Exception {
        // given
        MyNotPersistedAgent agent = mock(MyNotPersistedAgent.class, Answers.RETURNS_DEEP_STUBS);
        given(context.getAgent()).willReturn(agent);

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.ABORT);
    }

    abstract static class MyPersistedAgent implements Agent, PersistentLogin {}

    abstract static class MyNotPersistedAgent implements Agent {
        public final Class<? extends Agent> getAgentClass() {
            return getClass();
        }
    }
}
