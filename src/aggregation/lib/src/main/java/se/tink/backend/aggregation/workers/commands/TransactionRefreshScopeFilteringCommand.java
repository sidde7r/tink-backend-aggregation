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
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;

@Slf4j
@RequiredArgsConstructor
public class TransactionRefreshScopeFilteringCommand extends AgentWorkerCommand {

    private static final Toggle TOGGLE = Toggle.of("TransactionsRefreshScope").build();

    private final UnleashClient unleashClient;
    private final AccountDataCache accountDataCache;
    private final TransactionsRefreshScope transactionsRefreshScope;

    public TransactionRefreshScopeFilteringCommand(
            UnleashClient unleashClient,
            AccountDataCache accountDataCache,
            CredentialsRequest request) {
        this(unleashClient, accountDataCache, getTransactionRefreshScopeFromRequest(request));
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        boolean toggleValue = unleashClient.isToggleEnable(TOGGLE);
        if (toggleValue && transactionsRefreshScope != null) {
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
