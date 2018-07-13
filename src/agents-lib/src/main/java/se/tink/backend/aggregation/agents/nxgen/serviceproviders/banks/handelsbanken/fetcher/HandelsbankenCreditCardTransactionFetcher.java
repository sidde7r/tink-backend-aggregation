package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class HandelsbankenCreditCardTransactionFetcher implements TransactionFetcher<CreditCardAccount> {
    private final HandelsbankenApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;

    public HandelsbankenCreditCardTransactionFetcher(HandelsbankenApiClient client,
            HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        return sessionStorage.creditCards()
                .flatMap(creditCards -> creditCards.find(account))
                .map(creditCard -> client.creditCardTransactions(creditCard)
                        .tinkTransactions(creditCard, account))
                .orElse(Collections.emptyList());
    }
}
