package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class HandelsbankenAccountTransactionFetcher implements TransactionFetcher<TransactionalAccount> {
    private final HandelsbankenApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;

    public HandelsbankenAccountTransactionFetcher(HandelsbankenApiClient client,
            HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        return sessionStorage.accountList()
                .flatMap(accountList -> accountList.find(account))
                .map(handelsbankenAccount -> client.transactions(handelsbankenAccount)
                        .toTinkTransactions(account, client, sessionStorage))
                .orElse(Collections.emptyList());
    }
}
