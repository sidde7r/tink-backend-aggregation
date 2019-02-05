package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

public class RevolutTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {
    private final RevolutApiClient apiClient;

    public RevolutTransactionFetcher(RevolutApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        int count = RevolutConstants.Pagination.COUNT;
        String toDateMillis = (key != null ? key : Long.toString(System.currentTimeMillis()));

        TransactionKeyPaginatorResponseImpl<String> response =
                new TransactionKeyPaginatorResponseImpl<>();

        String accountCurrency = account.getFromTemporaryStorage(RevolutConstants.Storage.CURRENCY);
        String accountBankId = account.getBankIdentifier();

        if (account.getType().equals(AccountTypes.CHECKING)) {
            Collection<TransactionEntity> transactionEntities =
                    apiClient.fetchTransactions(count, toDateMillis);

            response.setTransactions(
                    transactionEntities
                            .stream()
                            .filter(TransactionEntity::isValid)
                            .filter(t -> t.hasCurrency(accountCurrency))
                            .filter(
                                    t ->
                                            t.isTopUp()
                                                    || t.isCardPayment()
                                                    || t.belongsToAccount(accountBankId))
                            .map(TransactionEntity::toTinkTransaction)
                            .collect(Collectors.toList()));

            response.setNext(
                    transactionEntities
                            .stream()
                            .map(TransactionEntity::getStartedDate)
                            .min(Comparator.comparing(t -> t))
                            .map(t -> Long.toString(t))
                            .orElse(null));

            return response;

        } else if (account.getType().equals(AccountTypes.SAVINGS)) {
            Collection<TransactionEntity> transactionEntities =
                    apiClient.fetchTransactions(count, toDateMillis);

            response.setTransactions(
                    transactionEntities
                            .stream()
                            .filter(TransactionEntity::isValid)
                            .filter(t -> t.hasCurrency(accountCurrency))
                            .filter(t -> t.belongsToAccount(accountBankId))
                            .map(TransactionEntity::toTinkTransaction)
                            .collect(Collectors.toList()));

            response.setNext(
                    transactionEntities
                            .stream()
                            .map(TransactionEntity::getStartedDate)
                            .min(Comparator.comparing(t -> t))
                            .map(t -> Long.toString(t))
                            .orElse(null));
            return response;
        }
        return response;
    }
}
