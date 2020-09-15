package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.fetcher;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.EdenredApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.EdenredConstants;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.storage.EdenredStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class EdenredTransactionsFetcher implements TransactionFetcher<TransactionalAccount> {

    private final EdenredApiClient edenredApiClient;

    private final EdenredStorage edenredStorage;

    public EdenredTransactionsFetcher(
            EdenredApiClient edenredApiClient, EdenredStorage edenredStorage) {
        this.edenredApiClient = edenredApiClient;
        this.edenredStorage = edenredStorage;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        long apiId = Long.parseLong(account.getApiIdentifier());

        TransactionsEntity transactionsEntity =
                edenredStorage
                        .getTransactions(apiId)
                        .orElseGet(() -> edenredApiClient.getTransactions(apiId).getData());

        try {
            return transactionsEntity.getMovementList().stream()
                    .map(this::mapTransaction)
                    .collect(Collectors.toList());
        } finally {
            edenredStorage.cleanTransactions(apiId);
        }
    }

    private AggregationTransaction mapTransaction(TransactionEntity transactionEntity) {
        final ExactCurrencyAmount amount =
                ExactCurrencyAmount.of(transactionEntity.getAmount(), EdenredConstants.CURRENCY);

        return Transaction.builder()
                .setDescription(transactionEntity.getTransactionName())
                .setDate(transactionEntity.getTransactionDate())
                .setAmount(amount)
                .build();
    }
}
