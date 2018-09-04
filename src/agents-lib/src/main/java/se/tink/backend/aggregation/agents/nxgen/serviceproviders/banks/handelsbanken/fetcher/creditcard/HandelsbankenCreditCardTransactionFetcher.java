package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
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

        // Fetch transactions for creditcards that are accounts.
        List<AggregationTransaction> transactions = sessionStorage.accountList()
                .flatMap(accountList -> accountList.find(account))
                .filter(HandelsbankenAccount::isCreditCard)
                .map(handelsbankenAccount -> client.transactions(handelsbankenAccount)
                        .toTinkTransactions(account, client, sessionStorage))
                .orElse(Collections.emptyList());

        transactions.addAll(sessionStorage.creditCards()
                .flatMap(creditCards -> creditCards.find(account))
                .map(creditCard -> client.creditCardTransactions(creditCard)
                        .tinkTransactions(creditCard, account))
                .orElse(Collections.emptyList())
        );

        return transactions;
    }
}
