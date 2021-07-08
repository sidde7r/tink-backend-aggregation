package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.workers.commands.state.InstantiateAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public class InstantiateAgentWorkerCommandTest {

    private InstantiateAgentWorkerCommand command;
    private AgentWorkerCommandContext context;
    private InstantiateAgentWorkerCommandState state;

    @Before
    public void setUp() {
        context = mock(AgentWorkerCommandContext.class, Answers.RETURNS_DEEP_STUBS);
        state = mock(InstantiateAgentWorkerCommandState.class, Answers.RETURNS_DEEP_STUBS);
        command = new InstantiateAgentWorkerCommand(context, state);
    }

    @Test
    public void doExecutionShouldInitStateInitContextAndContinue() throws Exception {
        // given
        given(context.getRequest().getProvider().getName()).willReturn("provider-name");
        given(context.getRequest().getCredentials().getId()).willReturn("credentials-id");
        // and
        Agent agent = mock(Agent.class);
        given(state.getAgentFactory().create(context.getRequest(), context)).willReturn(agent);

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        verify(state).doRightBeforeInstantiation("provider-name", "credentials-id");

        verify(context).setAgent(agent);
    }

    @Test
    public void doPostProcessShouldPostProcessTheState() throws Exception {
        // given

        // when
        command.doPostProcess();

        // then
        verify(state).doAtInstantiationPostProcess();
    }
}
