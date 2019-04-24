package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.creditcard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenFICreditCard;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenFIAccountTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.HandelsbankenCreditCardTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenPaginatorResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.URL;

public class HandelsbankenFICreditCardTransactionFetcher
        extends HandelsbankenCreditCardTransactionPaginator<HandelsbankenFIApiClient> {
    private final AggregationLogger log =
            new AggregationLogger(HandelsbankenFICreditCardTransactionFetcher.class);

    public HandelsbankenFICreditCardTransactionFetcher(
            HandelsbankenFIApiClient client, HandelsbankenSessionStorage sessionStorage) {
        super(client, sessionStorage);
    }

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(
            CreditCardAccount account, URL key) {

        /*
         * Try pagination implementation, which is only tested for HandelsbankenSE.
         * If it fails we fall back on the old implementation.
         *
         * If this is verified to work then this class can be completely removed.
         */
        try {

            TransactionKeyPaginatorResponse<URL> response = super.getTransactionsFor(account, key);
            log.info("Pagination seems to be working. Remove this class.");
            return response;
        } catch (Exception e) {

            log.error("Pagination failed, paginator is likely incorrectly implemented.", e);
            return new HandelsbankenPaginatorResponse(
                    fetchTransactionsFor(account), Optional.empty());
        }
    }

    @SuppressWarnings("unchecked")
    public List<Transaction> fetchTransactionsFor(CreditCardAccount account) {
        // Fetch transactions for creditcards that are accounts.
        // They will have both account transactions and credit card transactions.
        List<Transaction> accountTransactions =
                sessionStorage
                        .accountList()
                        .flatMap(accountList -> accountList.find(account))
                        .filter(HandelsbankenAccount::isCreditCard)
                        .map(
                                handelsbankenAccount -> {
                                    // Fetch the account transactions and filter out the summary
                                    // transactions.
                                    List<Transaction> subTransactions =
                                            removeSummaryTransactions(
                                                    client.transactions(handelsbankenAccount)
                                                            .toTinkTransactions());
                                    // Fetch the card transactions.
                                    subTransactions.addAll(
                                            client.creditCardTransactions(
                                                            handelsbankenAccount
                                                                    .getCardTransactionsUrl())
                                                    .tinkTransactions(account));
                                    return subTransactions;
                                })
                        .orElseGet(Collections::emptyList);
        List<Transaction> transactions = new ArrayList<>(accountTransactions);
        List<CreditCardTransaction> creditCardTransactions =
                sessionStorage
                        .<HandelsbankenFICreditCard>creditCards()
                        .flatMap(creditCards -> creditCards.find(account))
                        .map(
                                creditCard ->
                                        client.creditCardTransactions(creditCard)
                                                .tinkTransactions(creditCard, account))
                        .orElseGet(Collections::emptyList);
        transactions.addAll(creditCardTransactions);

        return transactions;
    }

    private List<Transaction> removeSummaryTransactions(List<Transaction> transactions) {
        return transactions.stream()
                .filter(
                        transaction ->
                                !HandelsbankenConstants.TransactionFiltering.CREDIT_CARD_SUMMARY
                                        .equalsIgnoreCase(transaction.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    protected List<Transaction> fetchAccountTransactions(
            HandelsbankenAccount handelsbankenAccount) {

        HandelsbankenFIAccountTransactionPaginator paginator =
                new HandelsbankenFIAccountTransactionPaginator(client, sessionStorage);

        List<Transaction> transactions = new ArrayList<>();
        URL nextKey = null;
        do {

            TransactionKeyPaginatorResponse<URL> response =
                    paginator.getTransactionsFor(handelsbankenAccount, nextKey);
            transactions.addAll(response.getTinkTransactions());
            nextKey = response.nextKey();

        } while (nextKey != null);

        return transactions;
    }
}
