package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.storage.database.daos.CryptoConfigurationDao;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.encryption.CredentialsCrypto;
import se.tink.backend.aggregation.wrappers.CryptoWrapper;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.aggregation.storage.database.repositories.ClusterCryptoConfigurationRepository;

public class EncryptCredentialsWorkerCommand extends AgentWorkerCommand {

    private final AgentWorkerCommandContext context;
    private final CredentialsCrypto credentialsCrypto;
    private final boolean doUpdateCredential;

    public EncryptCredentialsWorkerCommand(ClusterInfo clusterInfo, CacheClient cacheClient,
            ClusterCryptoConfigurationRepository clusterCryptoConfigurationRepository,
            AgentWorkerCommandContext context, ControllerWrapper controllerWrapper, CryptoWrapper cryptoWrapper) {
        this(clusterInfo, cacheClient, clusterCryptoConfigurationRepository,
                context, true, controllerWrapper, cryptoWrapper);
    }

    public EncryptCredentialsWorkerCommand(ClusterInfo clusterInfo, CacheClient cacheClient,
            ClusterCryptoConfigurationRepository clusterCryptoConfigurationRepository,
            AgentWorkerCommandContext context, boolean doUpdateCredential,
            ControllerWrapper controllerWrapper, CryptoWrapper cryptoWrapper) {
        this.context = context;
        this.doUpdateCredential = doUpdateCredential;
        this.credentialsCrypto = new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper);
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
