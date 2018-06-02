package se.tink.backend.aggregation.workers.commands;

import com.google.common.base.Preconditions;
import se.tink.backend.common.repository.mysql.aggregation.AggregationCredentialsRepository;
import se.tink.backend.core.AggregationCredentials;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.encryption.api.EncryptionService;
import se.tink.backend.encryption.rpc.EncryptionKeySet;

@Deprecated
public class EncryptAgentWorkerCommand extends AgentWorkerCommand {
    private EncryptAgentWorkerCommandState state;
    private AgentWorkerContext context;
    private boolean overwriteSecretKey = false;

    public EncryptAgentWorkerCommand(AgentWorkerContext context, EncryptAgentWorkerCommandState state) {
        this.context = context;
        this.state = state;
    }

    public EncryptAgentWorkerCommand withOverwriteSecretKey(boolean overwriteSecretKey) {
        this.overwriteSecretKey = overwriteSecretKey;
        return this;
    }

    public static class EncryptAgentWorkerCommandState {
        private AggregationCredentialsRepository aggregationCredentialsRepository;

        public EncryptAgentWorkerCommandState(ServiceContext serviceContext) {
            aggregationCredentialsRepository = serviceContext.getRepository(AggregationCredentialsRepository.class);
        }

        public AggregationCredentialsRepository getAggregationCredentialsRepository() {
            return aggregationCredentialsRepository;
        }
    }

    @Override
    public AgentWorkerCommandResult execute() {
        Credentials credentials = context.getRequest().getCredentials();

        // Delete any existing aggregation credentials.

        AggregationCredentials aggregationCredentials =
                state.getAggregationCredentialsRepository().findOne(credentials.getId());

        if (aggregationCredentials != null) {
            state.getAggregationCredentialsRepository().delete(aggregationCredentials);
        }

        // Create a new set of keys.

        EncryptionService encryptionService = context.getServiceContext().getEncryptionServiceFactory()
                .getEncryptionService();

        // Generate the two AES secret keys that we store in the databases.

        EncryptionKeySet keySet = encryptionService.generateEncryptionKeys();

        // Validate keys just in case.

        Preconditions.checkState(keySet.getKeys().size() == 2);

        for (String key : keySet.getKeys()) {
            Preconditions.checkNotNull(key);
        }

        // Save one secret key locally.

        aggregationCredentials = new AggregationCredentials(credentials.getId());
        aggregationCredentials.setSecretKey(keySet.getKeys().get(0));
        state.getAggregationCredentialsRepository().save(aggregationCredentials);

        // Update main's credential secretKey only if it's not set before.
        // We do this to avoid a race condition that can occur if main updates its secretKey at the same
        // time another request decrypts using the old key.
        if (!overwriteSecretKey && credentials.getSecretKey() == null) {
            credentials.setSecretKey(keySet.getKeys().get(1));
        }

        context.updateCredentialsOnlySensitiveInformation(credentials);

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }
}
