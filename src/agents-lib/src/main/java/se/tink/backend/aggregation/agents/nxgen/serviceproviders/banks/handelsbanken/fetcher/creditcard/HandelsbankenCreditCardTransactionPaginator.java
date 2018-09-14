package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenCreditCard;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenPaginatorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.URL;

public class HandelsbankenCreditCardTransactionPaginator implements
        TransactionKeyPaginator<CreditCardAccount, URL> {
    private final HandelsbankenApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;

    public HandelsbankenCreditCardTransactionPaginator(
            HandelsbankenApiClient client,
            HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(CreditCardAccount account,
            URL key) {

        if(key != null){
            CreditCardTransactionsResponse<HandelsbankenCreditCard> response = client.creditCardTransactions(key);
            return new HandelsbankenPaginatorResponse(
                    response.tinkTransactions(account),
                    response.getPaginationKey());
        }

        return getCreditAccountTransactions(account)
                .orElse(getCreditCardTransactions(account)
                        .orElse(null));
    }

    private Optional<HandelsbankenPaginatorResponse> getCreditAccountTransactions(CreditCardAccount account){

        return sessionStorage.accountList()
                .flatMap(accountList -> accountList.find(account))
                .filter(HandelsbankenAccount::isCreditCard)
                .map(handelsbankenAccount -> {
                    // Fetch the account transactions and filter out the summary transactions.
                    List<Transaction> subTransactions = removeSummaryTransactions(
                            fetchAccountTransactions(handelsbankenAccount));

                    // Fetch the card transactions.
                    subTransactions.addAll(
                            client.creditCardTransactions(handelsbankenAccount.toCardTransactions())
                                    .tinkTransactions(account)
                    );

                    CreditCardTransactionsResponse<HandelsbankenCreditCard> response =
                            client.creditCardTransactions(handelsbankenAccount.toCardTransactions());

                    HandelsbankenPaginatorResponse paginatorResponse =
                            new HandelsbankenPaginatorResponse(
                                    response.tinkTransactions(account),
                                    response.getPaginationKey());
                    paginatorResponse.addAll(subTransactions);

                    return paginatorResponse;
                });
    }

    private Optional<HandelsbankenPaginatorResponse> getCreditCardTransactions(CreditCardAccount account){

        return sessionStorage.creditCards()
                .flatMap(creditCards -> creditCards.find(account))
                .map(card -> {
                    CreditCardTransactionsResponse response = client.creditCardTransactions(card);
                    return new HandelsbankenPaginatorResponse(
                            response.tinkTransactions(card, account),
                            response.getPaginationKey());
                });
    }

    private List<Transaction> fetchAccountTransactions(HandelsbankenAccount handelsbankenAccount){
        return client.transactions(handelsbankenAccount).toTinkTransactions();
    }

    private List<Transaction> removeSummaryTransactions(List<Transaction> transactions) {
        return transactions.stream()
                .filter(transaction -> !HandelsbankenConstants.TransactionFiltering.CREDIT_CARD_SUMMARY
                        .equalsIgnoreCase(transaction.getDescription()))
                .collect(Collectors.toList());
    }
}
