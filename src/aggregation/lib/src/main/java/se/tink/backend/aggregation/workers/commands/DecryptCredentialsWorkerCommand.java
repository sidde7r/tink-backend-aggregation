package se.tink.backend.aggregation.workers.commands;

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

    public DecryptCredentialsWorkerCommand(
            AgentWorkerCommandContext context, CredentialsCrypto credentialsCrypto) {
        this.context = context;
        this.credentialsCrypto = credentialsCrypto;
        this.doUpdateCredential = true;
    }

    public DecryptCredentialsWorkerCommand(
            AgentWorkerCommandContext context,
            CredentialsCrypto credentialsCrypto,
            boolean doUpdateCredential) {
        this.context = context;
        this.credentialsCrypto = credentialsCrypto;
        this.doUpdateCredential = doUpdateCredential;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        CredentialsRequest request = context.getRequest();

        didDecryptCredential = credentialsCrypto.decrypt(request);
        if (!didDecryptCredential) {
            throw new IllegalStateException("Could not decrypt credential");
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        if (!didDecryptCredential) {
            // Do not encrypt it again since we didn't do anything with it.
            return;
        }

        // Encrypt credential again.
        CredentialsRequest request = context.getRequest();
        credentialsCrypto.encrypt(request, doUpdateCredential);
    }
}
