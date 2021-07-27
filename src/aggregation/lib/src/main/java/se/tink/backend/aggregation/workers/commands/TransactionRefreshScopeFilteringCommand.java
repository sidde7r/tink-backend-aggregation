package se.tink.backend.aggregation.workers.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.account_data_cache.AccountDataCache;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.HasRefreshScope;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.TransactionsRefreshScope;

@Slf4j
@RequiredArgsConstructor
public class TransactionRefreshScopeFilteringCommand extends AgentWorkerCommand {

    private final AccountDataCache accountDataCache;
    private final TransactionsRefreshScope transactionsRefreshScope;

    public TransactionRefreshScopeFilteringCommand(
            AccountDataCache accountDataCache, CredentialsRequest request) {
        this(accountDataCache, getTransactionRefreshScopeFromRequest(request));
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        if (transactionsRefreshScope != null) {
            accountDataCache.setAccountTransactionDateLimit(
                    transactionsRefreshScope::getTransactionBookedDateGteForAccountIdentifiers);
        }
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {}

    private static TransactionsRefreshScope getTransactionRefreshScopeFromRequest(
            CredentialsRequest request) {
        TransactionsRefreshScope transactionsRefreshScope = null;
        if (request instanceof HasRefreshScope) {
            RefreshScope refreshScope = ((HasRefreshScope) request).getRefreshScope();
            if (refreshScope != null) {
                transactionsRefreshScope = refreshScope.getTransactions();
            }
        }
        return transactionsRefreshScope;
    }
}
