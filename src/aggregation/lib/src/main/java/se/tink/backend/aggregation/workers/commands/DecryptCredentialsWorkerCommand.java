package se.tink.backend.aggregation.workers.commands;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.encryption.CredentialsCrypto;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class DecryptCredentialsWorkerCommand extends AgentWorkerCommand {
    private final AgentWorkerCommandContext context;
    private final CredentialsCrypto credentialsCrypto;
    private final boolean doUpdateCredential;

    private boolean didDecryptCredential = false;
    private Charset charset;

    public DecryptCredentialsWorkerCommand(
            AgentWorkerCommandContext context, CredentialsCrypto credentialsCrypto) {
        this.context = context;
        this.credentialsCrypto = credentialsCrypto;
        this.doUpdateCredential = true;
        this.charset = StandardCharsets.UTF_8;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        CredentialsRequest request = context.getRequest();

        didDecryptCredential = credentialsCrypto.decrypt(request, charset);
        if (!didDecryptCredential) {
            throw new IllegalStateException("Could not decrypt credential");
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        if (!didDecryptCredential) {
            // Do not encrypt it again since we didn't do anything with it.
            return;
        }

        // Encrypt credential again.
        CredentialsRequest request = context.getRequest();
        credentialsCrypto.encrypt(request, doUpdateCredential);
    }
}
