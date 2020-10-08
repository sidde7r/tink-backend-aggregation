package se.tink.backend.aggregation.workers.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public class ClearSensitivePayloadOnForceAuthenticateCommand extends AgentWorkerCommand {
    private static final Logger log =
            LoggerFactory.getLogger(ClearSensitivePayloadOnForceAuthenticateCommand.class);

    private final AgentWorkerCommandContext context;

    public ClearSensitivePayloadOnForceAuthenticateCommand(AgentWorkerCommandContext context) {
        this.context = context;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        try {
            if (context.getAgent() instanceof PersistentLogin
                    && context.getRequest().isForceAuthenticate()) {

                log.info(
                        "Credentials contain - isForceAuthenticate: {}",
                        context.getRequest().isForceAuthenticate());
                log.info("Clearing session to force authentication towards the bank");

                // Nuke Sensitive Storage
                context.getRequest().getCredentials().setSensitivePayloadAsMap(null);
            }
        } catch (RuntimeException clearSensitivePayload) {
            log.warn("Could not clear sensitive payload", clearSensitivePayload);
        }
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        // NOP
    }
}
