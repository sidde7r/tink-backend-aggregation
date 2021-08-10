package se.tink.backend.aggregation.workers.commands;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.utils.mappers.CoreCredentialsMapper;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;

@Slf4j
public class UpdateCredentialsStatusAgentWorkerCommand extends AgentWorkerCommand {

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

        CredentialsStatusInfoUtlis.logCredentialsInfo(credentials);

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
        CredentialsStatusInfoUtlis.logCredentialsInfo(credentials);

        if (FAILED_STATUSES.contains(credentials.getStatus())) {
            log.info(
                    "[UPDATE] Credentials status does not warrant status update - Status: {}",
                    credentials.getStatus());
            return;
        }

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
