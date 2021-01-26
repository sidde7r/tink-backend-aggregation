package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.ProviderStatuses;
import se.tink.backend.aggregation.agents.utils.mappers.CoreCredentialsMapper;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;

/**
 * Prevents the command chain to advance if provider has one of the not allowed statuses
 *
 * <p>Also does the clean up to ensure credentials object is again in a state where other command
 * chains can be triggered after this one.
 *
 * <p>TODO: consider moving the cleaning up to `postProcess` if possible + desired
 *
 * <p>TODO: consider renaming the command
 */
public class ValidateProviderAgentWorkerStatus extends AgentWorkerCommand {
    private AgentWorkerCommandContext context;
    private final ControllerWrapper controllerWrapper;
    private final BiMap<ProviderStatuses, CredentialsStatus> blacklistedProviderStatuses;

    public ValidateProviderAgentWorkerStatus(
            AgentWorkerCommandContext context, ControllerWrapper controllerWrapper) {
        this(
                context,
                controllerWrapper,
                ImmutableBiMap.<ProviderStatuses, CredentialsStatus>builder()
                        .put(ProviderStatuses.TEMPORARY_DISABLED, CredentialsStatus.UNCHANGED)
                        .put(ProviderStatuses.DISABLED, CredentialsStatus.PERMANENT_ERROR)
                        .build());
    }

    ValidateProviderAgentWorkerStatus(
            AgentWorkerCommandContext context,
            ControllerWrapper controllerWrapper,
            BiMap<ProviderStatuses, CredentialsStatus> blacklistedProviderStatuses) {
        this.context = context;
        this.controllerWrapper = controllerWrapper;
        this.blacklistedProviderStatuses = blacklistedProviderStatuses;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        Provider provider = context.getRequest().getProvider();

        if (blacklistedProviderStatuses.containsKey(provider.getStatus())) {
            updateCredentialStatusForBlacklistedProviderStatuses();
            return AgentWorkerCommandResult.ABORT;
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    void updateCredentialStatusForBlacklistedProviderStatuses() {
        Provider provider = context.getRequest().getProvider();
        Credentials credentials = context.getRequest().getCredentials();
        Optional<String> refreshId = context.getRefreshId();

        credentials.setStatus(blacklistedProviderStatuses.get(provider.getStatus()));
        credentials.clearSensitiveInformation(provider);

        se.tink.libraries.credentials.rpc.Credentials coreCredentials =
                CoreCredentialsMapper.fromAggregationCredentials(credentials);

        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest
                updateCredentialsStatusRequest =
                        new se.tink.backend.aggregation.aggregationcontroller.v1.rpc
                                .UpdateCredentialsStatusRequest();

        updateCredentialsStatusRequest.setCredentials(coreCredentials);
        updateCredentialsStatusRequest.setUserId(credentials.getUserId());
        updateCredentialsStatusRequest.setRequestType(context.getRequest().getType());
        updateCredentialsStatusRequest.setOperationId(context.getRequest().getOperationId());
        refreshId.ifPresent(updateCredentialsStatusRequest::setRefreshId);

        controllerWrapper.updateCredentials(updateCredentialsStatusRequest);
    }

    @Override
    protected void doPostProcess() throws Exception {
        // Deliberately left empty.
    }
}
