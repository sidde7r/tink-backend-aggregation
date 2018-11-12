package se.tink.backend.aggregation.workers.commands;

import java.util.Objects;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.rpc.ProviderStatuses;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.common.mapper.CoreCredentialsMapper;

public class ValidateProviderAgentWorkerStatus extends AgentWorkerCommand {
    private AgentWorkerCommandContext context;
    private final ControllerWrapper controllerWrapper;

    public ValidateProviderAgentWorkerStatus(AgentWorkerCommandContext context, ControllerWrapper controllerWrapper) {
        this.context = context;
        this.controllerWrapper = controllerWrapper;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        Provider provider = context.getRequest().getProvider();
        Credentials credentials = context.getRequest().getCredentials();

        if (!Objects.equals(provider.getStatus(), ProviderStatuses.TEMPORARY_DISABLED) &&
                !Objects.equals(provider.getStatus(), ProviderStatuses.DISABLED)) {
            return AgentWorkerCommandResult.CONTINUE;
        }

        credentials.setStatus(CredentialsStatus.UNCHANGED);
        credentials.clearSensitiveInformation(provider);

        // TODO: Refactor System API side to not depend on :main-api
        se.tink.backend.core.Credentials coreCredentials = CoreCredentialsMapper
                .fromAggregationCredentials(credentials);

        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest updateCredentialsStatusRequest =
                new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest();

        updateCredentialsStatusRequest.setCredentials(coreCredentials);
        updateCredentialsStatusRequest.setUserId(credentials.getUserId());

        controllerWrapper.updateCredentials(updateCredentialsStatusRequest);

        return AgentWorkerCommandResult.ABORT;
    }

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }
}
