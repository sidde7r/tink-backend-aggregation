package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.transactionalaccount;


import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenPaginatorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.URL;

public class HandelsbankenFIAccountTransactionPaginator implements TransactionKeyPaginator<TransactionalAccount, URL> {

    private final HandelsbankenApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;

    public HandelsbankenFIAccountTransactionPaginator(HandelsbankenApiClient client,
            HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(TransactionalAccount account,
            URL key) {

        if(key != null){
            TransactionsResponse response = client.transactions(key);
            return new HandelsbankenPaginatorResponse(
                    response.toTinkTransactions(),
                    response.getPaginationKey());
        }

         return sessionStorage.accountList()
                .flatMap(accountList -> accountList.find(account))
                .map(handelsbankenAccount -> client.transactions(handelsbankenAccount))
                 .map(response -> new HandelsbankenPaginatorResponse(
                         response.toTinkTransactions(),
                         response.getPaginationKey()))
                 .orElse(null);
    }
}
