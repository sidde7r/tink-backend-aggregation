package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenSEAccountTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.HandelsbankenCreditCardTransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class HandelsbankenSECreditCardTransactionPaginator extends
        HandelsbankenCreditCardTransactionPaginator<HandelsbankenSEApiClient> {

    private static final int NUM_TRANSACTIONS_PER_FETCH = 30;

    public HandelsbankenSECreditCardTransactionPaginator(
            HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage) {
        super(client, sessionStorage);
    }

    @Override
    protected List<Transaction> fetchAccountTransactions(
            HandelsbankenAccount handelsbankenAccount) {

        HandelsbankenSEAccountTransactionPaginator paginator = new HandelsbankenSEAccountTransactionPaginator(
                client, sessionStorage);

        List<Transaction> transactions = new ArrayList<>();
        int startIndex = 0;
        while (true) {

            Collection<? extends Transaction> fetched = paginator
                    .getTransactionsFor(handelsbankenAccount,
                            NUM_TRANSACTIONS_PER_FETCH, startIndex).getTinkTransactions();

            transactions.addAll(fetched);

            if (fetched.size() < NUM_TRANSACTIONS_PER_FETCH)
                break;

            startIndex += NUM_TRANSACTIONS_PER_FETCH;
        }

        return transactions;
    }
}
