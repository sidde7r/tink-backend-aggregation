package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.account_data_cache.AccountDataCache;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.HasRefreshScope;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.TransactionsRefreshScope;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;

public class TransactionRefreshScopeFilteringCommand extends AgentWorkerCommand {

    private static final Toggle TOGGLE = Toggle.of("TransactionsRefreshScope").build();

    private final UnleashClient unleashClient;
    private final AccountDataCache accountDataCache;
    private final TransactionsRefreshScope transactionsRefreshScope;

    public TransactionRefreshScopeFilteringCommand(
            UnleashClient unleashClient,
            AccountDataCache accountDataCache,
            CredentialsRequest request) {
        this.unleashClient = unleashClient;
        this.accountDataCache = accountDataCache;
        if (request instanceof HasRefreshScope) {
            RefreshScope refreshScope = ((HasRefreshScope) request).getRefreshScope();
            transactionsRefreshScope = refreshScope != null ? refreshScope.getTransactions() : null;
        } else {
            transactionsRefreshScope = null;
        }
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        if (unleashClient.isToggleEnable(TOGGLE) && transactionsRefreshScope != null) {
            accountDataCache.setAccountTransactionDateLimit(
                    transactionsRefreshScope::getTransactionBookedDateGteForAccountIdentifiers);
        }
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {}
}
