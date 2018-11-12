package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.encryption.CredentialsCrypto;
import se.tink.backend.aggregation.wrappers.CryptoWrapper;
import se.tink.backend.common.cache.CacheClient;

public class DecryptCredentialsWorkerCommand extends AgentWorkerCommand {
    private final AgentWorkerCommandContext context;
    private final CredentialsCrypto credentialsCrypto;

    private boolean didDecryptCredential = false;

    public DecryptCredentialsWorkerCommand(CacheClient cacheClient, AgentWorkerCommandContext context,
            ControllerWrapper controllerWrapper, CryptoWrapper cryptoWrapper,
            CredentialsCrypto credentialsCrypto) {
        this.context = context;
        this.credentialsCrypto = credentialsCrypto;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        CredentialsRequest request = context.getRequest();

        didDecryptCredential = credentialsCrypto.decrypt(request);
        if (!didDecryptCredential) {
            return AgentWorkerCommandResult.ABORT;
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
        credentialsCrypto.encrypt(request, true);
    }
}
