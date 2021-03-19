package se.tink.backend.aggregation.workers.commands;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.encryption.CredentialsCrypto;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class EncryptCredentialsWorkerCommand extends AgentWorkerCommand {

    private final AgentWorkerCommandContext context;
    private final CredentialsCrypto credentialsCrypto;
    private final boolean doUpdateCredential;
    private Charset charset;

    public EncryptCredentialsWorkerCommand(
            AgentWorkerCommandContext context, CredentialsCrypto credentialsCrypto) {
        this(context, true, credentialsCrypto);
    }

    public EncryptCredentialsWorkerCommand(
            AgentWorkerCommandContext context,
            boolean doUpdateCredential,
            CredentialsCrypto credentialsCrypto) {
        this.context = context;
        this.doUpdateCredential = doUpdateCredential;
        this.credentialsCrypto = credentialsCrypto;

        this.charset = null;

        CredentialsRequest request = context.getRequest();
        if (request != null) {
            Provider provider = request.getProvider();

            if (provider != null && "at-test-password".equalsIgnoreCase(provider.getName())) {
                charset = StandardCharsets.UTF_8;
            }
        }
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        CredentialsRequest request = context.getRequest();

        if (!credentialsCrypto.encrypt(request, doUpdateCredential, charset)) {
            throw new IllegalStateException("Could not encrypt credential");
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        // Purposely left empty.
    }
}
