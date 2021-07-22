package se.tink.backend.aggregation.workers.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.AgentWorkerContext;
import se.tink.libraries.account_data_cache.AccountDataCache;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.HasRefreshScope;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.TransactionsRefreshScope;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;

@RequiredArgsConstructor
public class TransactionRefreshScopeFilteringCommand extends AgentWorkerCommand {

    private static final Set<String> TEST_APP_IDS =
            Collections.unmodifiableSet(
                    new HashSet<>(
                            Arrays.asList(
                                    "ef2d8c482ad54ec99811ec79f7207e66", // Piotr Miecielica
                                    // production
                                    "3a44573c3bb94913bbada637a6ca41ee" // Jacek Garbulinski staging
                                    )));
    private static final Toggle TOGGLE = Toggle.of("TransactionsRefreshScope").build();

    private final AgentWorkerContext context;
    private final UnleashClient unleashClient;
    private final AccountDataCache accountDataCache;
    private final TransactionsRefreshScope transactionsRefreshScope;

    public TransactionRefreshScopeFilteringCommand(
            AgentWorkerContext context,
            UnleashClient unleashClient,
            AccountDataCache accountDataCache,
            CredentialsRequest request) {
        this(
                context,
                unleashClient,
                accountDataCache,
                getTransactionRefreshScopeFromRequest(request));
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        if (unleashClient.isToggleEnable(TOGGLE)
                && TEST_APP_IDS.contains(context.getAppId())
                && transactionsRefreshScope != null) {
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
