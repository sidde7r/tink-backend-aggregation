package se.tink.backend.aggregation.workers.commands;

import java.util.Objects;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.agents.utils.mappers.CoreCredentialsMapper;

public class UpdateCredentialsStatusAgentWorkerCommand extends AgentWorkerCommand {
    private static final Logger log = LoggerFactory.getLogger(UpdateCredentialsStatusAgentWorkerCommand.class);
    private final ControllerWrapper controllerWrapper;
    private final Credentials credentials;
    private final Provider provider;
    private final AgentWorkerCommandContext context;
    private final Predicate<AgentWorkerCommandContext> setStatusUpdatedPredicate;

    public UpdateCredentialsStatusAgentWorkerCommand(ControllerWrapper controllerWrapper,
            Credentials credentials, Provider provider, AgentWorkerCommandContext context,
            Predicate<AgentWorkerCommandContext> setStatusUpdatedPredicate) {
        this.controllerWrapper = controllerWrapper;
        this.credentials = credentials;
        this.provider = provider;
        this.context = context;
        this.setStatusUpdatedPredicate = setStatusUpdatedPredicate;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        if (Objects.equals(CredentialsStatus.AUTHENTICATING, credentials.getStatus())) {
            return AgentWorkerCommandResult.CONTINUE;
        }

        updateStatus(CredentialsStatus.AUTHENTICATING);

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        if (!setStatusUpdatedPredicate.test(context)) {
            return;
        }

        if (CredentialsStatus.FAILED_OPERATION_STATUSES.contains(credentials.getStatus())) {
            log.info("Credentials status does not warrant status update - Status: {}", credentials.getStatus());
            return;
        }

        log.info("Updating credentials status to UPDATED - Current status: {}", credentials.getStatus());
        updateStatus(CredentialsStatus.UPDATED);
    }

    private void updateStatus(CredentialsStatus newStatus) {
        credentials.setStatus(newStatus);

        Credentials credentialsCopy = credentials.clone();
        credentialsCopy.clearSensitiveInformation(provider);

        UpdateCredentialsStatusRequest updateCredentialsStatusRequest = new UpdateCredentialsStatusRequest();
        updateCredentialsStatusRequest.setCredentials(
                CoreCredentialsMapper.fromAggregationCredentials(credentialsCopy));

        controllerWrapper.updateCredentials(updateCredentialsStatusRequest);
    }
}
