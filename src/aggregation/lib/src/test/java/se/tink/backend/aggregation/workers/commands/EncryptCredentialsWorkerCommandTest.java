package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.encryption.CredentialsCrypto;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public class EncryptCredentialsWorkerCommandTest {

    private EncryptCredentialsWorkerCommand command;
    private AgentWorkerCommandContext context;
    private CredentialsCrypto credentialsCrypto;

    @Before
    public void setUp() {
        context = mock(AgentWorkerCommandContext.class, Answers.RETURNS_DEEP_STUBS);
        credentialsCrypto = mock(CredentialsCrypto.class, Answers.RETURNS_DEEP_STUBS);
        command = new EncryptCredentialsWorkerCommand(context, credentialsCrypto);
    }

    @Test
    public void doExecuteShouldThrowExceptionWhenEncryptWasUnsuccessfull() {
        // given
        given(credentialsCrypto.encrypt(context.getRequest(), true)).willReturn(false);

        // when
        Throwable t = catchThrowable(() -> command.doExecute());

        // then
        assertThat(t)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not encrypt credential");
    }

    @Test
    public void doExecuteShouldContinueIfCredentialsWereEncrypted() throws Exception {
        // given
        given(credentialsCrypto.encrypt(context.getRequest(), true, StandardCharsets.UTF_8))
                .willReturn(true);

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
    }
}
