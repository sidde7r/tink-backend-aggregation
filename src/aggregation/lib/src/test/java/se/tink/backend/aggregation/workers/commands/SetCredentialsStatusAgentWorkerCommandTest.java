package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;

public class SetCredentialsStatusAgentWorkerCommandTest {
    private SetCredentialsStatusAgentWorkerCommand command;
    private AgentWorkerCommandContext context;

    private CredentialsStatus status;

    @Before
    public void setUp() {
        context = mock(AgentWorkerCommandContext.class, Answers.RETURNS_DEEP_STUBS);
        status = CredentialsStatus.AUTHENTICATING;
        command = new SetCredentialsStatusAgentWorkerCommand(context, status);
    }

    @Test
    public void doExecuteShouldUpdateStatusAndContinue() throws Exception {
        // given

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        verify(context).updateStatus(status);
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
    }
}
