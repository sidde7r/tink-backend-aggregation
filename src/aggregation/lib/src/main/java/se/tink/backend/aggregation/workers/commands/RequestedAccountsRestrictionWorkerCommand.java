package se.tink.backend.aggregation.workers.commands;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.account_data_cache.FilterReason;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;

public class RequestedAccountsRestrictionWorkerCommand extends AgentWorkerCommand {
    private static final Logger log =
            LoggerFactory.getLogger(RequestedAccountsRestrictionWorkerCommand.class);
    private final AgentWorkerCommandContext context;

    public RequestedAccountsRestrictionWorkerCommand(AgentWorkerCommandContext context) {
        this.context = context;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        if (!supportIndividualAccountsRefresh()) {
            return AgentWorkerCommandResult.CONTINUE;
        }
        try {
            CredentialsRequest request = context.getRequest();
            if (request instanceof RefreshInformationRequest) {
                Set<String> getRequestedAccountIds =
                        ((RefreshInformationRequest) request).getRequestedAccountIds();
                registerAccountFilter(getRequestedAccountIds);
            }
        } catch (RuntimeException e) {
            log.warn("Could not execute RequestedAccountsRestrictionWorkerCommand", e);
        }
        return AgentWorkerCommandResult.CONTINUE;
    }

    private boolean supportIndividualAccountsRefresh() {
        return context.getAgentsServiceConfiguration()
                .isFeatureEnabled("supportIndividualAccountsRefresh");
    }

    private void registerAccountFilter(Set<String> requestedAccountIds) {
        log.info(
                "Applying account restriction filter for credentialsId: {}, requested accountIds: {}",
                context.getRequest().getCredentials().getId(),
                requestedAccountIds);
        this.context
                .getAccountDataCache()
                .addFilter(
                        account -> requestedAccountIds.contains(account.getId()),
                        FilterReason.GRANULAR_REFRESH_REQUESTED_ACCOUNTS);
    }

    @Override
    protected void doPostProcess() throws Exception {
        // Intentionally left empty.
    }
}
