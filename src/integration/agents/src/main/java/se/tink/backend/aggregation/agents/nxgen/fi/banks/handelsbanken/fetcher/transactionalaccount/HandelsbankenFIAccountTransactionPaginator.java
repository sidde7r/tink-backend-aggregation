package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.URL;

public class HandelsbankenFIAccountTransactionPaginator
        implements TransactionKeyPaginator<TransactionalAccount, URL> {

    private final HandelsbankenFIApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;

    public HandelsbankenFIAccountTransactionPaginator(
            HandelsbankenFIApiClient client, HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(
            TransactionalAccount account, URL key) {

        return sessionStorage
                .accountList()
                .flatMap(accountList -> accountList.find(account))
                .map(handelsbankenAccount -> getTransactionsFor(handelsbankenAccount, key))
                .orElse(null);
    }

    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(
            HandelsbankenAccount account, URL key) {

        if (key != null) {
            return client.transactions(key);
        }

        return client.transactions(account);
    }
}
