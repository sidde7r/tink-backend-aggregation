package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
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
        // They will have both account transactions and credit card transactions.
        List<AggregationTransaction> accountTransactions = sessionStorage.accountList()
                .flatMap(accountList -> accountList.find(account))
                .filter(HandelsbankenAccount::isCreditCard)
                .map(handelsbankenAccount -> {
                    // Fetch the account transactions and filter out the summary transactions.
                    List<AggregationTransaction> subTransactions = removeSummaryTransactions(
                            client.transactions(handelsbankenAccount).toTinkTransactions(account, client,
                                    sessionStorage));

                    // Fetch the card transactions.
                    subTransactions.addAll(
                            client.creditCardTransactions(handelsbankenAccount.toCardTransactions())
                                    .tinkTransactions(account)
                    );

                    return subTransactions;
                })
                .orElse(Collections.emptyList());

        List<AggregationTransaction> transactions = new ArrayList<>(accountTransactions);

        List<AggregationTransaction> creditCardTransactions = sessionStorage.creditCards()
                .flatMap(creditCards -> creditCards.find(account))
                .map(creditCard -> client.creditCardTransactions(creditCard)
                        .tinkTransactions(creditCard, account))
                .orElse(Collections.emptyList());

        transactions.addAll(creditCardTransactions);

        return transactions;
    }

    private List<AggregationTransaction> removeSummaryTransactions(List<AggregationTransaction> transactions) {
        return transactions.stream()
                .filter(transaction -> !HandelsbankenConstants.TransactionFiltering.CREDIT_CARD_SUMMARY
                        .equalsIgnoreCase(transaction.getDescription()))
                .collect(Collectors.toList());
    }
}
