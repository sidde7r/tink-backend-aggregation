package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;

public class ClearSensitiveInformationCommand extends AgentWorkerCommand {

    private final AgentWorkerContext context;

    public ClearSensitiveInformationCommand(AgentWorkerContext context) {
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
