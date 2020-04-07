package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.ProviderStatuses;
import se.tink.backend.aggregation.agents.utils.mappers.CoreCredentialsMapper;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

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
    private final Set<ProviderStatuses> blacklistedProviderStatuses;

    public ValidateProviderAgentWorkerStatus(
            AgentWorkerCommandContext context, ControllerWrapper controllerWrapper) {
        this(
                context,
                controllerWrapper,
                ImmutableSet.of(ProviderStatuses.DISABLED, ProviderStatuses.TEMPORARY_DISABLED));
    }

    ValidateProviderAgentWorkerStatus(
            AgentWorkerCommandContext context,
            ControllerWrapper controllerWrapper,
            Set<ProviderStatuses> blacklistedProviderStatuses) {
        this.context = context;
        this.controllerWrapper = controllerWrapper;
        this.blacklistedProviderStatuses = blacklistedProviderStatuses;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        Provider provider = context.getRequest().getProvider();

        if (blacklistedProviderStatuses.contains(provider.getStatus())) {
            updateCredentialStatusToUnchanged();
            return AgentWorkerCommandResult.ABORT;
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    void updateCredentialStatusToUnchanged() {
        Provider provider = context.getRequest().getProvider();
        Credentials credentials = context.getRequest().getCredentials();
        Optional<String> refreshId = context.getRefreshId();

        credentials.setStatus(CredentialsStatus.UNCHANGED);
        credentials.clearSensitiveInformation(provider);

        se.tink.libraries.credentials.rpc.Credentials coreCredentials =
                CoreCredentialsMapper.fromAggregationCredentials(credentials);

        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest
                updateCredentialsStatusRequest =
                        new se.tink.backend.aggregation.aggregationcontroller.v1.rpc
                                .UpdateCredentialsStatusRequest();

        updateCredentialsStatusRequest.setCredentials(coreCredentials);
        updateCredentialsStatusRequest.setUserId(credentials.getUserId());
        refreshId.ifPresent(updateCredentialsStatusRequest::setRefreshId);

        controllerWrapper.updateCredentials(updateCredentialsStatusRequest);
    }

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }
}
