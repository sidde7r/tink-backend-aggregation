package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities.TransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class WizinkTransactionFetcher implements TransactionFetcher<TransactionalAccount> {

    private WizinkApiClient wizinkApiClient;
    private WizinkStorage wizinkStorage;

    public WizinkTransactionFetcher(WizinkApiClient wizinkApiClient, WizinkStorage wizinkStorage) {
        this.wizinkApiClient = wizinkApiClient;
        this.wizinkStorage = wizinkStorage;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        String internalKey = account.getApiIdentifier();
        boolean firstFullRefresh = wizinkStorage.getFirstFullRefreshFlag();

        TransactionResponse transactionsFrom90Days =
                wizinkApiClient.fetchTransactionsFrom90Days(internalKey).getTransactionResponse();

        List<AggregationTransaction> transactions = transactionsFrom90Days.getTransactions();
        Optional<String> sessionId = getSessionId(internalKey);
        if (firstFullRefresh
                && transactionsFrom90Days.canFetchTransactionsOlderThan90Days()
                && sessionId.isPresent()) {
            transactions.addAll(getTransactionsFromMoreThan90Days(internalKey, sessionId.get()));
        }
        return transactions;
    }

    private List<AggregationTransaction> getTransactionsFromMoreThan90Days(
            String internalKey, String sessionId) {
        return wizinkApiClient
                .fetchTransactionsOlderThan90Days(sessionId, internalKey)
                .getTransactionResponse()
                .getTransactions();
    }

    private Optional<String> getSessionId(String internalKey) {
        return wizinkApiClient
                .fetchSessionIdForOlderAccountTransactions(internalKey)
                .getTransactionResponse()
                .getSessionEntity()
                .getSessionId();
    }
}
