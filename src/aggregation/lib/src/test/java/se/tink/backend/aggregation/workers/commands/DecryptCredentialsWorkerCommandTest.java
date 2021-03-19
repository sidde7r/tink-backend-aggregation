package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.encryption.CredentialsCrypto;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class DecryptCredentialsWorkerCommandTest {

    private DecryptCredentialsWorkerCommand command;
    private AgentWorkerCommandContext context;
    private CredentialsCrypto credentialsCrypto;

    @Before
    public void setUp() {
        context = mock(AgentWorkerCommandContext.class, Answers.RETURNS_DEEP_STUBS);
        credentialsCrypto = mock(CredentialsCrypto.class, Answers.RETURNS_DEEP_STUBS);
        command = new DecryptCredentialsWorkerCommand(context, credentialsCrypto);
    }

    @Test
    public void doExceuteShouldThrowExceptionWhenDidNotDecryptCredentials() {
        // given
        given(credentialsCrypto.decrypt(context.getRequest())).willReturn(false);

        // when
        Throwable t = catchThrowable(() -> command.doExecute());

        // then
        assertThat(t)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not decrypt credential");
    }

    @Test
    public void doExecuteShouldContinueWhenCredentialsAreDecrypted() throws Exception {
        // given
        given(credentialsCrypto.decrypt(context.getRequest(), null)).willReturn(true);

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void doPostProcessShouldNotEncryptIfCredsWereNotDecrypted() throws Exception {
        // given

        // when
        command.doPostProcess();

        // then
        verifyZeroInteractions(credentialsCrypto);
    }

    @Test
    public void doPostProcessShouldEncryptIfCredsWereDecryptedSuccessfully() throws Exception {
        // given
        given(credentialsCrypto.decrypt(context.getRequest(), null)).willReturn(true);
        command.doExecute();

        // when
        command.doPostProcess();

        // then
        verify(credentialsCrypto).encrypt(any(CredentialsRequest.class), eq(true));
    }
}
