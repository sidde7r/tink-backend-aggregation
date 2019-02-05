package se.tink.backend.aggregation.workers.commands;

import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.encryption.CredentialsCrypto;

public class EncryptCredentialsWorkerCommand extends AgentWorkerCommand {

    private final AgentWorkerCommandContext context;
    private final CredentialsCrypto credentialsCrypto;
    private final boolean doUpdateCredential;

    public EncryptCredentialsWorkerCommand(AgentWorkerCommandContext context, CredentialsCrypto credentialsCrypto) {
        this(context, true, credentialsCrypto);
    }

    public EncryptCredentialsWorkerCommand(AgentWorkerCommandContext context, boolean doUpdateCredential,
            CredentialsCrypto credentialsCrypto) {
        this.context = context;
        this.doUpdateCredential = doUpdateCredential;
        this.credentialsCrypto = credentialsCrypto;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        CredentialsRequest request = context.getRequest();

        if (!credentialsCrypto.encrypt(request, doUpdateCredential)) {
            return AgentWorkerCommandResult.ABORT;
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        // Purposely left empty.
    }
}
