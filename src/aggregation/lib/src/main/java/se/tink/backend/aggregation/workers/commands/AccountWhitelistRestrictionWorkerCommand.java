package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.rpc.ConfigureWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.WhitelistRequest;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.account_data_cache.FilterReason;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.enums.TinkFeature;

public class AccountWhitelistRestrictionWorkerCommand extends AgentWorkerCommand {
    private final AgentWorkerCommandContext context;
    private final CredentialsRequest refreshInformationRequest;

    public AccountWhitelistRestrictionWorkerCommand(
            AgentWorkerCommandContext context, CredentialsRequest request) {
        this.context = context;
        this.refreshInformationRequest = request;
    }

    // Remove any account that is flagged as excluded.
    private boolean filterExcludedAccounts(Account account) {
        return !account.getAccountExclusion().excludedFeatures.contains(TinkFeature.AGGREGATION);
    }

    // Remove any account that was not present in the original refreshRequest (i.e. whitelisted).
    private boolean filterNonWhitelistedAccounts(Account account) {
        List<Account> accountsFromRequest =
                refreshInformationRequest.getAccounts() == null
                        ? Lists.newArrayList()
                        : refreshInformationRequest.getAccounts();

        return accountsFromRequest.stream()
                .anyMatch(
                        accountFromRequest ->
                                accountFromRequest.getBankId().equals(account.getBankId()));
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        // Always filter excluded accounts.
        this.context
                .getAccountDataCache()
                .addFilter(this::filterExcludedAccounts, FilterReason.ACCOUNT_EXCLUSION_FEATURE);

        if (!(refreshInformationRequest instanceof WhitelistRequest)) {
            // No additional filtering for non-whitelist requests.
            return AgentWorkerCommandResult.CONTINUE;
        }

        if (refreshInformationRequest instanceof ConfigureWhitelistInformationRequest) {
            // Whitelist configuration (handled in `RequestUserOptInAccountsAgentWorkerCommand`)
            // implements its own account filtering.
            // We cannot evaluate the accounts present in the refreshRequest for such
            // requests!
            return AgentWorkerCommandResult.CONTINUE;
        }

        this.context
                .getAccountDataCache()
                .addFilter(this::filterNonWhitelistedAccounts, FilterReason.OPT_IN);

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        // Intentionally left empty.
    }
}
