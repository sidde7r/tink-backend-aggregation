package se.tink.backend.aggregation.workers.commands;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;

@RunWith(MockitoJUnitRunner.class)
public class CreateAgentConfigurationControllerWorkerCommandTest {
    private CreateAgentConfigurationControllerWorkerCommand command;
    private AgentWorkerCommandContext context;

    @Before
    public void setUp() {
        context = mock(AgentWorkerCommandContext.class, Answers.RETURNS_DEEP_STUBS);
        TppSecretsServiceClient tppSecretsServiceClient =
                mock(TppSecretsServiceClient.class, Answers.RETURNS_DEEP_STUBS);

        command =
                new CreateAgentConfigurationControllerWorkerCommand(
                        context, tppSecretsServiceClient);
    }

    @Test
    public void
            doPostProcessShouldCallCompleteSecretSubjectValuesOnNonNullAgentConfigurationController()
                    throws Exception {
        // given
        AgentConfigurationControllerable agent = mock(AgentConfigurationControllerable.class);
        // and
        Field agentConfigurationControllerField =
                CreateAgentConfigurationControllerWorkerCommand.class.getDeclaredField(
                        "agentConfigurationController");
        agentConfigurationControllerField.setAccessible(true);
        agentConfigurationControllerField.set(command, agent);

        // when
        command.doPostProcess();

        // then
        verify(agent).completeSecretValuesSubject();
    }
}
