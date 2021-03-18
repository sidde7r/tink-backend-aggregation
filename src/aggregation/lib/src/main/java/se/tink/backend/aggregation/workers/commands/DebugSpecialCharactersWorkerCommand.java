package se.tink.backend.aggregation.workers.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class DebugSpecialCharactersWorkerCommand extends AgentWorkerCommand {

    private static final Logger log =
            LoggerFactory.getLogger(DebugSpecialCharactersWorkerCommand.class);

    private final AgentWorkerCommandContext context;
    private final String debugAdditionalInfo;
    private boolean execute = false;

    public DebugSpecialCharactersWorkerCommand(
            AgentWorkerCommandContext context, final String debugAdditionalInfo) {
        this.context = context;
        this.debugAdditionalInfo = debugAdditionalInfo;

        CredentialsRequest request = context.getRequest();
        if (request != null) {
            Provider provider = request.getProvider();

            if (provider != null && "at-test-password".equalsIgnoreCase(provider.getName())) {
                execute = true;
            }
        }
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        try {
            if (execute) {
                CredentialsRequest request = context.getRequest();
                Credentials credentials = request.getCredentials();

                int fieldsSerializedHascode = 0;
                if (credentials.getFieldsSerialized() != null) {
                    fieldsSerializedHascode = credentials.getFieldsSerialized().hashCode();
                }

                log.info(
                        "[pre][{}] credentialsId {}  fieldSeralized hascode {}",
                        debugAdditionalInfo,
                        credentials.getId(),
                        fieldsSerializedHascode);
                credentials
                        .getFields()
                        .forEach(
                                (k, v) ->
                                        log.info(
                                                "[pre][{}] key: {} value hascode: {}",
                                                debugAdditionalInfo,
                                                k,
                                                v.hashCode()));
            }
        } catch (Exception e) {
            log.warn("Error with debugging", e);
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        // do nothing
    }
}
