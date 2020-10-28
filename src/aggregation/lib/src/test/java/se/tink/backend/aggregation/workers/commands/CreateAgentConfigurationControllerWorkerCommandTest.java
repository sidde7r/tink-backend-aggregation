package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.Provider.AccessType;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;
import se.tink.libraries.provider.ProviderDto.ProviderTypes;

@RunWith(MockitoJUnitRunner.class)
public class CreateAgentConfigurationControllerWorkerCommandTest {
    private CreateAgentConfigurationControllerWorkerCommand command;
    private AgentWorkerCommandContext context;

    @Before
    public void setUp() {
        context = mock(AgentWorkerCommandContext.class, Answers.RETURNS_DEEP_STUBS);
        TppSecretsServiceClient tppSecretsServiceClient =
                mock(TppSecretsServiceClient.class, Answers.RETURNS_DEEP_STUBS);
        Provider provider = mock(Provider.class, Answers.RETURNS_DEEP_STUBS);

        given(provider.getFinancialInstitutionId()).willReturn("sample financial institution id");
        given(provider.getName()).willReturn("sample provider name");
        given(provider.getAccessType()).willReturn(AccessType.OPEN_BANKING);
        given(provider.getType()).willReturn(ProviderTypes.BANK);

        given(context.getRequest().getProvider()).willReturn(provider);
        given(context.getAppId()).willReturn("sample application id");
        given(context.getClusterId()).willReturn("cluster id");
        given(context.getRequest().getCallbackUri()).willReturn("http://callback.uri");

        command =
                new CreateAgentConfigurationControllerWorkerCommand(
                        context, tppSecretsServiceClient);
    }

    @Test
    public void doExecuteShouldShouldAddAgentConfigurationControllerToContext() throws Exception {
        // given

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        // and
        verify(context).setAgentConfigurationController(captor.capture());
        assertThat(captor.getValue()).isNotNull();
    }

    @Captor private ArgumentCaptor<AgentConfigurationControllerable> captor;

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
