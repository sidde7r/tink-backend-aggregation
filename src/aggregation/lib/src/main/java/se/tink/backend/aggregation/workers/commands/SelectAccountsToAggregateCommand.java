package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.SetAccountsToAggregateContext;
import se.tink.backend.aggregation.rpc.ConfigureWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.WhitelistRequest;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.enums.TinkFeature;

public class SelectAccountsToAggregateCommand extends AgentWorkerCommand {
    private static final Logger log =
            LoggerFactory.getLogger(RequestUserOptInAccountsAgentWorkerCommand.class);
    private final SetAccountsToAggregateContext context;
    private final CredentialsRequest refreshInformationRequest;

    public SelectAccountsToAggregateCommand(
            SetAccountsToAggregateContext context, CredentialsRequest request) {
        this.context = context;
        this.refreshInformationRequest = request;
    }

    // select from all accounts that is under this credential and store in `accountsToAggregate`
    // list in
    // AgentWorkerCommandContext:
    //      if it is regular refresh (refresh all but excluded account:
    //          we store all besides the excluded accounts
    //      if it is a white listed refresh (refresh without asking user to select)
    //          we store only the white listed account (prefiously selected accounts)
    //      if it is a opt-in refresh (user select which accounts to aggregate
    //          we store only the selected accounts
    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        List<Account> allAccounts = context.getCachedAccounts();
        List<String> uniqueIdOfUserSelectedAccounts = context.getUniqueIdOfUserSelectedAccounts();
        List<Account> accountsFromRequest =
                refreshInformationRequest.getAccounts() == null
                        ? Lists.newArrayList()
                        : refreshInformationRequest.getAccounts();

        // handle black list removal
        List<Account> allExceptForBlacklisted =
                allAccounts.stream()
                        .filter(x -> !shouldNotAggregateDataForAccount(accountsFromRequest, x))
                        .collect(Collectors.toList());
        if (!(refreshInformationRequest instanceof WhitelistRequest)) {
            context.setAccountsToAggregate(allExceptForBlacklisted);
            return AgentWorkerCommandResult.CONTINUE;
        }

        // handle opt-in inclusion
        if (refreshInformationRequest instanceof ConfigureWhitelistInformationRequest) {
            context.setAccountsToAggregate(
                    allExceptForBlacklisted.stream()
                            .filter(a -> uniqueIdOfUserSelectedAccounts.contains(a.getBankId()))
                            .collect(Collectors.toList()));
            return AgentWorkerCommandResult.CONTINUE;
        }

        // handle whitelist-only inclusion
        List<String> accountIdsFromRequest =
                accountsFromRequest.stream().map(Account::getBankId).collect(Collectors.toList());
        context.setAccountsToAggregate(
                allExceptForBlacklisted.stream()
                        .filter(a -> accountIdsFromRequest.contains(a.getBankId()))
                        .collect(Collectors.toList()));
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {}

    private boolean shouldNotAggregateDataForAccount(
            List<Account> accountsFromRequest, Account account) {
        Optional<Account> existingAccount =
                accountsFromRequest.stream()
                        .filter(a -> Objects.equals(a.getBankId(), account.getBankId()))
                        .findFirst();

        return existingAccount.isPresent()
                && existingAccount
                        .get()
                        .getAccountExclusion()
                        .excludedFeatures
                        .contains(TinkFeature.AGGREGATION);
    }
}
