package se.tink.backend.aggregation.workers.commands;

import com.sun.jersey.api.client.UniformInterfaceException;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.backend.common.repository.mysql.aggregation.AggregationCredentialsRepository;
import se.tink.backend.core.AggregationCredentials;
import se.tink.backend.encryption.api.EncryptionService;
import se.tink.backend.encryption.rpc.DecryptionRequest;
import se.tink.backend.encryption.rpc.EncryptionKeySet;
import se.tink.backend.utils.StringUtils;

public class DecryptForMigrationAgentWorkerCommand extends AgentWorkerCommand {
    private static final AggregationLogger log = new AggregationLogger(DecryptForMigrationAgentWorkerCommand.class);

    private final AgentWorkerContext context;
    private final AggregationCredentialsRepository aggregationCredentialsRepository;

    public DecryptForMigrationAgentWorkerCommand(AgentWorkerContext context) {
        this.context = context;
        this.aggregationCredentialsRepository = context.getServiceContext().getRepository(
                AggregationCredentialsRepository.class);
    }

    @Override
    public AgentWorkerCommandResult execute() {
        CredentialsRequest request = context.getRequest();

        Credentials credentials = request.getCredentials();

        AggregationCredentials aggregationCredentials = aggregationCredentialsRepository.findOne(credentials
                .getId());

        if (aggregationCredentials == null || credentials.isDemoCredentials()) {
            return AgentWorkerCommandResult.ABORT;
        }

        // Decrypt and add the masked credentials fields.

        DecryptionRequest decryptRequest = new DecryptionRequest();
        decryptRequest.setKeySet(new EncryptionKeySet(credentials.getSecretKey(),
                aggregationCredentials.getSecretKey()));

        try {
            EncryptionService encryptionService = context.getServiceContext().getEncryptionServiceFactory()
                    .getEncryptionService();

            if (StringUtils.trimToNull(aggregationCredentials.getEncryptedFields()) != null) {
                decryptRequest.setPayload(aggregationCredentials.getEncryptedFields());
                credentials.addSerializedFields(encryptionService.decrypt(decryptRequest).getPayload());
            }

            if (StringUtils.trimToNull(aggregationCredentials.getEncryptedPayload()) != null) {
                decryptRequest.setPayload(aggregationCredentials.getEncryptedPayload());
                credentials.setSensitivePayloadSerialized(encryptionService.decrypt(decryptRequest).getPayload());
            }

            return AgentWorkerCommandResult.CONTINUE;
        } catch (UniformInterfaceException e) {
            // Encryption service is sad.

            // Log first so other things can't screw up logging.

            log.error(
                    String.format(
                            "Could not decrypt credentials (%s:%s, userId: %s). Look in encryption service log for error.",
                            credentials.getId(), credentials.getStatus(),
                            credentials.getUserId()));

            // Setting AUTHENTICATION_ERROR here because it is highly likely that this is due a
            // BadPaddingException or IllegalBlockSizeException.

            context.updateStatus(CredentialsStatus.AUTHENTICATION_ERROR);

            return AgentWorkerCommandResult.ABORT;
        }
    }

    @Override
    public void postProcess() throws Exception {
        // NOP
    }
}
