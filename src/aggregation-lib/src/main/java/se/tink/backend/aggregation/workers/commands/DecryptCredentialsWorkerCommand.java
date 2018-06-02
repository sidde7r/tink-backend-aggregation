package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.backend.aggregation.workers.encryption.CredentialsCrypto;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.repository.mysql.aggregation.ClusterCryptoConfigurationRepository;

public class DecryptCredentialsWorkerCommand extends AgentWorkerCommand {
    private final AgentWorkerContext context;
    private final CredentialsCrypto credentialsCrypto;

    private boolean didDecryptCredential = false;

    public DecryptCredentialsWorkerCommand(ClusterInfo clusterInfo, CacheClient cacheClient,
            ClusterCryptoConfigurationRepository clusterCryptoConfigurationRepository,
            AggregationControllerAggregationClient aggregationControllerAggregationClient,
            AgentWorkerContext context) {
        this.context = context;
        this.credentialsCrypto = new CredentialsCrypto(clusterInfo, cacheClient,
                clusterCryptoConfigurationRepository, aggregationControllerAggregationClient);
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
