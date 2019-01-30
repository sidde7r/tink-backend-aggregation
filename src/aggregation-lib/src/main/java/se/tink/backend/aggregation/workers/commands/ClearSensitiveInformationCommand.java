package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.credentials_requests.CredentialsRequest;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;

public class ClearSensitiveInformationCommand extends AgentWorkerCommand {

    private final AgentWorkerCommandContext context;

    public ClearSensitiveInformationCommand(AgentWorkerCommandContext context) {
        this.context = context;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        CredentialsRequest request = context.getRequest();
        Provider provider = request.getProvider();

        request.getCredentials().clearSensitiveInformation(provider);
    }
}
