package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.configurations.dao.CryptoConfigurationDao;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.backend.aggregation.workers.encryption.CredentialsCrypto;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.aggregation.configurations.repositories.ClusterCryptoConfigurationRepository;

public class EncryptCredentialsWorkerCommand extends AgentWorkerCommand {

    private final AgentWorkerContext context;
    private final CredentialsCrypto credentialsCrypto;
    private final boolean doUpdateCredential;

    public EncryptCredentialsWorkerCommand(CacheClient cacheClient,
                                           ClusterCryptoConfigurationRepository clusterCryptoConfigurationRepository,
                                           AggregationControllerAggregationClient aggregationControllerAggregationClient,
                                           AgentWorkerContext context) {
        this(cacheClient, clusterCryptoConfigurationRepository, aggregationControllerAggregationClient,
                context, true);
    }

    public EncryptCredentialsWorkerCommand(CacheClient cacheClient,
            ClusterCryptoConfigurationRepository clusterCryptoConfigurationRepository,
            AggregationControllerAggregationClient aggregationControllerAggregationClient,
            AgentWorkerContext context, boolean doUpdateCredential) {
        this.context = context;
        this.doUpdateCredential = doUpdateCredential;
        credentialsCrypto = new CredentialsCrypto(
                new CryptoConfigurationDao(clusterCryptoConfigurationRepository), context.getHostConfiguration(), cacheClient,
                aggregationControllerAggregationClient);
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
