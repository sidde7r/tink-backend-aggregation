package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ClearSensitiveInformationCommandTest {

    private AgentWorkerCommandContext context;
    private ClearSensitiveInformationCommand command;

    @Before
    public void setUp() {
        context = mock(AgentWorkerCommandContext.class);
        command = new ClearSensitiveInformationCommand(context);
    }

    @Test
    public void doExecuteShouldReturnContinue() throws Exception {
        // given

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void postProcessShouldClearSensitiveInformationFromCredentials() throws Exception {
        // given
        CredentialsRequest request = mock(CredentialsRequest.class, Answers.RETURNS_DEEP_STUBS);
        Provider provider = mock(Provider.class);
        // and
        given(context.getRequest()).willReturn(request);
        given(request.getProvider()).willReturn(provider);

        // when
        command.postProcess();

        // then
        verify(request.getCredentials()).clearSensitiveInformation(provider);
    }
}
