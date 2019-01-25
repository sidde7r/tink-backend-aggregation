package se.tink.backend.aggregation.workers.commands;

import java.util.Objects;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.agents.utils.mappers.CoreCredentialsMapper;

public class SetCredentialsStatusToAuthenticatingAgentWorkerCommand extends AgentWorkerCommand {
    private final ControllerWrapper controllerWrapper;
    private final Credentials credentials;
    private final Provider provider;

    public SetCredentialsStatusToAuthenticatingAgentWorkerCommand(ControllerWrapper controllerWrapper,
            Credentials credentials, Provider provider) {
        this.controllerWrapper = controllerWrapper;
        this.credentials = credentials;
        this.provider = provider;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        if (Objects.equals(CredentialsStatus.AUTHENTICATING, credentials.getStatus())) {
            return AgentWorkerCommandResult.CONTINUE;
        }

        credentials.setStatus(CredentialsStatus.AUTHENTICATING);

        Credentials credentialsCopy = credentials.clone();
        credentialsCopy.clearSensitiveInformation(provider);

        UpdateCredentialsStatusRequest updateCredentialsStatusRequest = new UpdateCredentialsStatusRequest();
        updateCredentialsStatusRequest.setCredentials(
                CoreCredentialsMapper.fromAggregationCredentials(credentialsCopy));

        controllerWrapper.updateCredentials(updateCredentialsStatusRequest);
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        // NOT
    }
}
