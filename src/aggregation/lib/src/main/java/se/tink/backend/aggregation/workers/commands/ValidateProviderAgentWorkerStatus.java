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
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;

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

        if (!blacklistedProviderStatuses.contains(provider.getStatus())) {
            return AgentWorkerCommandResult.CONTINUE;
        }

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

        return AgentWorkerCommandResult.ABORT;
    }

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }
}
