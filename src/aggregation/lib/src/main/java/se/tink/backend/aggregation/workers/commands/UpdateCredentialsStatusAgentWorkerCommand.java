package se.tink.backend.aggregation.workers.commands;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.utils.mappers.CoreCredentialsMapper;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;

public class UpdateCredentialsStatusAgentWorkerCommand extends AgentWorkerCommand {
    private static final Logger log =
            LoggerFactory.getLogger(UpdateCredentialsStatusAgentWorkerCommand.class);
    private final ControllerWrapper controllerWrapper;
    private final Credentials credentials;
    private final Provider provider;
    private final AgentWorkerCommandContext context;
    private final Predicate<AgentWorkerCommandContext> setStatusUpdatedPredicate;

    private static final EnumSet<CredentialsStatus> FAILED_STATUSES =
            EnumSet.of(
                    CredentialsStatus.UNCHANGED,
                    CredentialsStatus.TEMPORARY_ERROR,
                    CredentialsStatus.AUTHENTICATION_ERROR);

    public UpdateCredentialsStatusAgentWorkerCommand(
            ControllerWrapper controllerWrapper,
            Credentials credentials,
            Provider provider,
            AgentWorkerCommandContext context,
            Predicate<AgentWorkerCommandContext> setStatusUpdatedPredicate) {
        this.controllerWrapper = controllerWrapper;
        this.credentials = credentials;
        this.provider = provider;
        this.context = context;
        this.setStatusUpdatedPredicate = setStatusUpdatedPredicate;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {

        log.info(
                "Credentials contain - supplemental Information: {}",
                credentials.getSupplementalInformation());
        log.info("Credentials contain - status payload: {}", credentials.getStatusPayload());
        log.info("Credentials contain - status: {}", credentials.getStatus());

        if (Objects.equals(CredentialsStatus.AUTHENTICATING, credentials.getStatus())) {
            return AgentWorkerCommandResult.CONTINUE;
        }

        updateStatus(CredentialsStatus.AUTHENTICATING);

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        if (!setStatusUpdatedPredicate.test(context)) {
            return;
        }

        log.info(
                "Credentials contain - supplemental Information: {}",
                credentials.getSupplementalInformation());
        log.info("Credentials contain - status payload: {}", credentials.getStatusPayload());

        log.info("Credentials contain - status: {}", credentials.getStatus());

        if (FAILED_STATUSES.contains(credentials.getStatus())) {
            log.info(
                    "Credentials status does not warrant status update - Status: {}",
                    credentials.getStatus());
            return;
        }

        log.info(
                "Updating credentials status to UPDATED - Current status: {}",
                credentials.getStatus());
        updateStatus(CredentialsStatus.UPDATED);
    }

    private void updateStatus(CredentialsStatus newStatus) {
        Optional<String> refreshId = context.getRefreshId();

        credentials.setStatus(newStatus);

        Credentials credentialsCopy = credentials.clone();
        credentialsCopy.clearSensitiveInformation(provider);

        UpdateCredentialsStatusRequest updateCredentialsStatusRequest =
                new UpdateCredentialsStatusRequest();
        updateCredentialsStatusRequest.setCredentials(
                CoreCredentialsMapper.fromAggregationCredentials(credentialsCopy));
        updateCredentialsStatusRequest.setRequestType(context.getRequest().getType());
        refreshId.ifPresent(updateCredentialsStatusRequest::setRefreshId);
        updateCredentialsStatusRequest.setOperationId(context.getRequest().getOperationId());

        controllerWrapper.updateCredentials(updateCredentialsStatusRequest);
    }
}
