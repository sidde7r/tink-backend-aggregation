package se.tink.backend.aggregation.workers.commands;

import com.sun.jersey.api.client.UniformInterfaceException;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.common.repository.mysql.aggregation.aggregationcredentials.AggregationCredentialsRepository;
import se.tink.backend.core.AggregationCredentials;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.backend.common.mapper.CoreCredentialsMapper;
import se.tink.backend.encryption.api.EncryptionService;
import se.tink.backend.encryption.rpc.DecryptionRequest;
import se.tink.backend.encryption.rpc.EncryptionKeySet;
import se.tink.backend.system.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.utils.StringUtils;

@Deprecated
public class DecryptAgentWorkerCommand extends AgentWorkerCommand {
    private static final AggregationLogger log = new AggregationLogger(DecryptAgentWorkerCommand.class);
    private AggregationCredentialsRepository aggregationCredentialsRepository;
    private AgentWorkerContext context;
    private boolean useAggregationController;
    private AggregationControllerAggregationClient aggregationControllerAggregationClient;

    private final boolean shouldEncryptCredential;
    private boolean hasDecryptedCredential = false;

    public DecryptAgentWorkerCommand(AgentWorkerContext context, boolean useAggregationController,
            AggregationControllerAggregationClient aggregationControllerAggregationClient) {
        this(context, useAggregationController, aggregationControllerAggregationClient, true);
    }

    public DecryptAgentWorkerCommand(AgentWorkerContext context, boolean useAggregationController,
            AggregationControllerAggregationClient aggregationControllerAggregationClient,
            boolean shouldEncryptCredential) {
        this.context = context;
        this.useAggregationController = useAggregationController;
        this.aggregationControllerAggregationClient = aggregationControllerAggregationClient;
        this.shouldEncryptCredential = shouldEncryptCredential;
        aggregationCredentialsRepository = context.getServiceContext().getRepository(
                AggregationCredentialsRepository.class);
    }

    @Override
    public AgentWorkerCommandResult execute() {
        CredentialsRequest request = context.getRequest();

        Credentials credentials = request.getCredentials();
        Provider provider = request.getProvider();

        AggregationCredentials aggregationCredentials = aggregationCredentialsRepository.findOne(credentials
                .getId());

        if (aggregationCredentials == null) {
            credentials.setStatus(CredentialsStatus.AUTHENTICATION_ERROR);
            credentials.clearSensitiveInformation(provider);

            // TODO: Refactor System API side to not depend on :main-api
            se.tink.backend.core.Credentials coreCredentials = CoreCredentialsMapper
                    .fromAggregationCredentials(credentials);

            if (useAggregationController) {
                se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest updateCredentialsStatusRequest =
                        new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest();

                updateCredentialsStatusRequest.setCredentials(coreCredentials);
                updateCredentialsStatusRequest.setUserId(request.getCredentials().getUserId());
                aggregationControllerAggregationClient.updateCredentials(updateCredentialsStatusRequest);
            } else {
                UpdateCredentialsStatusRequest updateCredentialsStatusRequest = new UpdateCredentialsStatusRequest();

                updateCredentialsStatusRequest.setCredentials(coreCredentials);
                updateCredentialsStatusRequest.setUserId(request.getCredentials().getUserId());

                context.getSystemServiceFactory().getUpdateService().updateCredentials(updateCredentialsStatusRequest);
            }

            return AgentWorkerCommandResult.ABORT;
        }

        if (credentials.isDemoCredentials()) {
            return AgentWorkerCommandResult.CONTINUE;
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

            hasDecryptedCredential = true;

            return AgentWorkerCommandResult.CONTINUE;
        } catch (UniformInterfaceException e) {
            // Encryption service is sad.

            // Log first so other things can't screw up logging.

            log.error(
                    String.format(
                            "Could not decrypt credentials (status: %s). Look in encryption service log for error.",
                            credentials.getStatus()));

            // Setting AUTHENTICATION_ERROR here because it is highly likely that this is due a
            // BadPaddingException or IllegalBlockSizeException.

            context.updateStatus(CredentialsStatus.AUTHENTICATION_ERROR);

            return AgentWorkerCommandResult.ABORT;
        }
    }

    @Override
    public void postProcess() throws Exception {
        if (!shouldEncryptCredential || !hasDecryptedCredential) {
            // Do not encrypt the credentials if
            //  1) The called instantiated this command with `shouldEncryptCredential` = false
            //  2) The credentials were never decrypted in the first place
            return;
        }

        CredentialsRequest request = context.getRequest();
        Credentials credentials = request.getCredentials();
        context.updateCredentialsOnlySensitiveInformation(credentials);
    }
}
