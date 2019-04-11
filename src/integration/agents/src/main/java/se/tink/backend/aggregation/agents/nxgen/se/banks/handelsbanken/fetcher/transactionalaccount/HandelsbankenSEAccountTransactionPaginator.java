package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class HandelsbankenSEAccountTransactionPaginator
        implements TransactionIndexPaginator<TransactionalAccount> {

    private final HandelsbankenSEApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;

    public HandelsbankenSEAccountTransactionPaginator(
            HandelsbankenSEApiClient client, HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, int numberOfTransactions, int startIndex) {

        // SHB sends all transaction in one request, do not try to fetch another batch
        if (startIndex > 0) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        return sessionStorage
                .accountList()
                .flatMap(accountList -> accountList.find(account))
                .map(client::transactions)
                .orElse(null);
    }
}
