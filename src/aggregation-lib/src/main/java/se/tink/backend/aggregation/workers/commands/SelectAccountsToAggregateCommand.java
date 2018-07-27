package se.tink.backend.aggregation.workers.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;
import se.tink.backend.aggregation.rpc.RefreshWhitelistInformationRequest;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.agents.SetAccountsToAggregateContext;
import se.tink.backend.core.enums.TinkFeature;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class SelectAccountsToAggregateCommand extends AgentWorkerCommand {
    private static final Logger log = LoggerFactory.getLogger(RequestUserOptInAccountsAgentWorkerCommand.class);
    private final SetAccountsToAggregateContext context;
    private final RefreshInformationRequest refreshInformationRequest;

    public SelectAccountsToAggregateCommand(SetAccountsToAggregateContext context, RefreshInformationRequest request) {
        this.context = context;
        this.refreshInformationRequest = request;
    }

    // refresh account and send supplemental information to system
    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        List<Account> allAccounts = context.getCachedAccounts();
        List<String> optInAccountNumbers = context.getAccountNumbersOfUserSelectedAccounts();
        List<Account> accountsFromRequest = refreshInformationRequest.getAccounts();

        List<Account> allExceptForBlacklisted = allAccounts.stream().filter(x -> !shouldNotAggregateDataForAccount(accountsFromRequest, x)).collect(Collectors.toList());

        if (!(refreshInformationRequest instanceof RefreshWhitelistInformationRequest)) {
            context.setAccountsToAggregate(allExceptForBlacklisted);
            return AgentWorkerCommandResult.CONTINUE;
        }

        RefreshWhitelistInformationRequest whiteListRequest = (RefreshWhitelistInformationRequest) refreshInformationRequest;
        if (whiteListRequest.isOptIn()) {
            context.setAccountsToAggregate(
                    // TODO: move from account number to bankId
                    allExceptForBlacklisted.stream().filter(a -> optInAccountNumbers.contains(a.getAccountNumber())).collect(Collectors.toList())
            );
            return AgentWorkerCommandResult.CONTINUE;
        }


        List<String> accountIdsFromRequest = accountsFromRequest.stream().map(Account::getBankId).collect(Collectors.toList());
        context.setAccountsToAggregate(
                allExceptForBlacklisted.stream().filter(a -> accountIdsFromRequest.contains(a.getBankId())).collect(Collectors.toList())
        );
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {

    }

    private boolean shouldNotAggregateDataForAccount(List<Account> accountsFromRequest, Account account) {
        Optional<Account> existingAccount = accountsFromRequest.stream()
                .filter(a -> Objects.equals(a.getBankId(), account.getBankId()))
                .findFirst();

        return existingAccount.isPresent() &&
                existingAccount.get().getAccountExclusion().excludedFeatures.contains(TinkFeature.AGGREGATION);
    }

}
