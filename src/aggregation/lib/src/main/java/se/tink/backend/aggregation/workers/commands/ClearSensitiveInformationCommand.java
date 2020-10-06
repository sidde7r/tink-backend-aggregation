package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ClearSensitiveInformationCommand extends AgentWorkerCommand {

    private final AgentWorkerCommandContext context;

    public ClearSensitiveInformationCommand(AgentWorkerCommandContext context) {
        this.context = context;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        CredentialsRequest request = context.getRequest();
        Provider provider = request.getProvider();

        request.getCredentials().clearSensitiveInformation(provider);
    }
}
