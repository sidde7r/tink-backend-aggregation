package se.tink.backend.aggregation.workers.commands;

import java.util.Date;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.connectivity.errors.ConnectivityErrorDetails.AuthorizationErrors;
import src.libraries.connectivity_errors.ConnectivityErrorFactory;

public class ExpireSessionAgentWorkerCommand extends AgentWorkerCommand {
    private static final Logger log =
            LoggerFactory.getLogger(ExpireSessionAgentWorkerCommand.class);

    private final boolean isUserAvailableForInteraction;
    private final StatusUpdater statusUpdater;
    private final Credentials credentials;
    private final Provider provider;

    public ExpireSessionAgentWorkerCommand(
            boolean isUserAvailableForInteraction,
            StatusUpdater statusUpdater,
            Credentials credentials,
            Provider provider) {
        this.isUserAvailableForInteraction = isUserAvailableForInteraction;
        this.statusUpdater = statusUpdater;
        this.credentials = credentials;
        this.provider = provider;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        if (isUserAvailableForInteraction) {
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

        ConnectivityError error =
                ConnectivityErrorFactory.authorizationError(AuthorizationErrors.SESSION_EXPIRED);
        statusUpdater.updateStatusWithError(CredentialsStatus.SESSION_EXPIRED, null, error);
        log.info("Successfully sent request to update credentials status to SESSION_EXPIRED.");

        return AgentWorkerCommandResult.ABORT;
    }

    @Override
    protected void doPostProcess() throws Exception {
        // NOP
    }
}
