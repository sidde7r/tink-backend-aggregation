package se.tink.backend.aggregation.workers.commands;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.mockito.Answers;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;

public class CreateAgentConfigurationControllerWorkerCommandTest {
    private CreateAgentConfigurationControllerWorkerCommand command;
    private AgentWorkerCommandContext context;
    private TppSecretsServiceClient tppSecretsServiceClient;

    @Before
    public void setUp() {
        context = mock(AgentWorkerCommandContext.class, Answers.RETURNS_DEEP_STUBS);
        tppSecretsServiceClient = mock(TppSecretsServiceClient.class, Answers.RETURNS_DEEP_STUBS);
        command =
                new CreateAgentConfigurationControllerWorkerCommand(
                        context, tppSecretsServiceClient);
    }
}
