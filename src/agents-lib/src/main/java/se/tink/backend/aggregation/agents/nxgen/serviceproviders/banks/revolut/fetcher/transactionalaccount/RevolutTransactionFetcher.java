package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class RevolutTransactionFetcher implements TransactionKeyPaginator<TransactionalAccount, String> {
    private final RevolutApiClient apiClient;

    public RevolutTransactionFetcher(RevolutApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(TransactionalAccount account, String key) {
        int count = RevolutConstants.Pagination.COUNT;
        String toDateMillis = (key != null ? key : Long.toString(System.currentTimeMillis()));

        TransactionKeyPaginatorResponseImpl<String> response = new TransactionKeyPaginatorResponseImpl<>();

        String accountCurrency = account.getFromTemporaryStorage(RevolutConstants.Storage.CURRENCY);
        String accountNumber = account.getAccountNumber();

        if (account.getType().equals(AccountTypes.CHECKING)) {
            Collection<TransactionEntity> transactionEntities = apiClient.fetchTransactions(count, toDateMillis);
            Stream<Transaction> combinedStream = Stream.concat(Stream.concat(
                    transactionEntities.stream()
                            .filter(t -> t.getCurrency().equalsIgnoreCase(accountCurrency))
                            .filter(t -> t.getAccount().isPresent())
                            .filter(t -> t.getAccount().get().getId().equalsIgnoreCase(accountNumber))
                            .map(t -> t.toTinkTransaction()),
                    transactionEntities.stream()
                            .filter(t -> t.getCurrency().equalsIgnoreCase(accountCurrency))
                            .filter(t -> t.isCardPayment())
                            .map(t -> t.toTinkTransaction())),
                    transactionEntities.stream()
                            .filter(t -> t.getCurrency().equalsIgnoreCase(accountCurrency))
                            .filter(t -> t.isTopUp())
                            .map(t -> t.toTinkTransaction())
                    );
            response.setTransactions(combinedStream.collect(Collectors.toList()));
            response.setNext(transactionEntities.stream()
                    .map(TransactionEntity::getStartedDate)
                    .min(Comparator.comparing(t -> t))
                    .map(t -> Long.toString(t))
                    .orElse(null)
            );

            return response;

        } else if (account.getType().equals(AccountTypes.SAVINGS)){
            Collection<TransactionEntity> transactionEntities = apiClient.fetchTransactions(count, toDateMillis);
            response.setTransactions(transactionEntities.stream()
                    .filter(t -> t.getCurrency().equalsIgnoreCase(accountCurrency))
                    .filter(t -> t.getAccount().isPresent())
                    .filter(t -> t.getAccount().get().getId().equalsIgnoreCase(accountNumber))
                    .map(transactionEntity -> transactionEntity.toTinkTransaction())
                    .collect(Collectors.toList())
            );
            response.setNext(transactionEntities.stream()
                    .map(TransactionEntity::getStartedDate)
                    .min(Comparator.comparing(t -> t))
                    .map(t -> Long.toString(t))
                    .orElse(null)
            );
            return response;
        }
            return response;

    }
}
