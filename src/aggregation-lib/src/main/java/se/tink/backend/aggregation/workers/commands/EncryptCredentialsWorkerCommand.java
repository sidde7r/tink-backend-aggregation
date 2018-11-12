package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.encryption.CredentialsCrypto;
import se.tink.backend.aggregation.wrappers.CryptoWrapper;
import se.tink.backend.common.cache.CacheClient;

public class EncryptCredentialsWorkerCommand extends AgentWorkerCommand {

    private final AgentWorkerCommandContext context;
    private final CredentialsCrypto credentialsCrypto;
    private final boolean doUpdateCredential;

    public EncryptCredentialsWorkerCommand(CacheClient cacheClient, AgentWorkerCommandContext context,
            ControllerWrapper controllerWrapper, CryptoWrapper cryptoWrapper,
            CredentialsCrypto credentialsCrypto) {
        this(cacheClient, context, true, controllerWrapper, cryptoWrapper, credentialsCrypto);
    }

    public EncryptCredentialsWorkerCommand(CacheClient cacheClient, AgentWorkerCommandContext context,
            boolean doUpdateCredential, ControllerWrapper controllerWrapper, CryptoWrapper cryptoWrapper,
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
