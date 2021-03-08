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

    /* This worker command checks if the incoming credentials request contains the flag for force authenticate or not.
     * It is essential that this command runs before the LoginAgentWorkerCommand in each place where LoginAgentWorkerCommand
     * has been used so that we can facilitate the force authentication feature  */

    private final AgentWorkerCommandContext context;

    public ClearSensitivePayloadOnForceAuthenticateCommand(AgentWorkerCommandContext context) {
        this.context = context;
    }

    @Override
    public AgentWorkerCommandResult doExecute() throws Exception {
        try {

            if (context.getAgent() instanceof PersistentLogin
                    && context.getRequest().shouldManualAuthBeForced()) {

                log.info(
                        "Credentials contain - isForceAuthenticate: {}",
                        context.getRequest().shouldManualAuthBeForced());
                log.info("Clearing session to force authentication towards the bank");

                // Nuke Sensitive Storage
                context.getRequest().getCredentials().setSensitivePayloadSerialized(null);
            }
        } catch (RuntimeException clearSensitivePayload) {
            log.warn("Could not clear sensitive payload", clearSensitivePayload);
        }
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void doPostProcess() throws Exception {
        // NOP
    }
}
