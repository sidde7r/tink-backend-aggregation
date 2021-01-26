package se.tink.backend.aggregation.workers.commands;

import java.util.Date;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;

public class ExpireSessionAgentWorkerCommand extends AgentWorkerCommand {
    private static final Logger log =
            LoggerFactory.getLogger(ExpireSessionAgentWorkerCommand.class);

    private final boolean isManualRefresh;
    private final SystemUpdater systemUpdater;
    private final Credentials credentials;
    private final Provider provider;

    public ExpireSessionAgentWorkerCommand(
            boolean isManualRefresh,
            SystemUpdater systemUpdater,
            Credentials credentials,
            Provider provider) {
        this.isManualRefresh = isManualRefresh;
        this.systemUpdater = systemUpdater;
        this.credentials = credentials;
        this.provider = provider;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        if (isManualRefresh) {
            return AgentWorkerCommandResult.CONTINUE;
        }

        if (Objects.isNull(provider.getAccessType())) {
            return AgentWorkerCommandResult.CONTINUE;
        }

        if (!Objects.equals(Provider.AccessType.OPEN_BANKING, provider.getAccessType())) {
            return AgentWorkerCommandResult.CONTINUE;
        }

        Date sessionExpiryDate = credentials.getSessionExpiryDate();

        if (Objects.isNull(sessionExpiryDate)) {
            return AgentWorkerCommandResult.CONTINUE;
        }

        Date now = new Date();

        if (now.before(sessionExpiryDate)) {
            return AgentWorkerCommandResult.CONTINUE;
        }

        log.info(
                "Expiring session - Date now: {}, Session expiry date: {}", now, sessionExpiryDate);
        credentials.setStatus(CredentialsStatus.SESSION_EXPIRED);
        systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, true);
        log.info("Successfully sent request to update credentials status to SESSION_EXPIRED.");

        return AgentWorkerCommandResult.ABORT;
    }

    @Override
    protected void doPostProcess() throws Exception {
        // NOP
    }
}
