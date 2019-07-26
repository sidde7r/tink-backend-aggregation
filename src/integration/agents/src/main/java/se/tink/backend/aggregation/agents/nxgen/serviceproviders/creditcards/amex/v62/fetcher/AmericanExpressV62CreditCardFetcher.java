package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.utils.AmericanExpressV62Storage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class AmericanExpressV62CreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final AmericanExpressV62Configuration configuration;
    private final AmericanExpressV62ApiClient apiClient;
    private final AmericanExpressV62Storage instanceStorage;

    private AmericanExpressV62CreditCardFetcher(
            AmericanExpressV62Configuration configuration,
            final AmericanExpressV62ApiClient apiClient,
            final AmericanExpressV62Storage instanceStorage) {
        this.apiClient = apiClient;
        this.configuration = configuration;
        this.instanceStorage = instanceStorage;
    }

    public static AmericanExpressV62CreditCardFetcher create(
            AmericanExpressV62Configuration config,
            AmericanExpressV62ApiClient apiClient,
            AmericanExpressV62Storage storage) {
        return new AmericanExpressV62CreditCardFetcher(config, apiClient, storage);
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<CardEntity> cardEntities = instanceStorage.getCreditCardList();

        Set<CreditCardAccount> accounts =
                cardEntities.stream()
                        .map(card -> card.toCreditCardAccount(configuration))
                        .collect(Collectors.toSet());

        List<CreditCardAccount> subAccountsList =
                fetchSubAccountsAndSavePendingTransactions(accounts);

        fetchAndStoreAllTransactions(accounts);

        accounts.addAll(subAccountsList);

        return accounts;
    }

    /**
     * This method takes the Set of Accounts and calls for timelineDetail for each of the account. A
     * valid @{@link TimelineResponse} contains a list of SubcardEntity and pending {@link
     * TransactionEntity}.
     *
     * <p>We save the pending transactions associated with this account in transactionList and use
     * the SubCardEntity list to create a map called 'completeSuppIndexAccountNumberMap'. This map
     * is later used to map each transaction to its particular card.
     *
     * @param accounts - accounts saved during loginsummary call.
     * @return list of subAccounts
     */
    private List<CreditCardAccount> fetchSubAccountsAndSavePendingTransactions(
            final Set<CreditCardAccount> accounts) {
        Map<String, HashMap<String, String>> completeSuppIndexAccountNumberMap =
                instanceStorage.getCompleteSubAccountsMap();
        Set<String> existingAccountNumbers =
                accounts.stream()
                        .map(CreditCardAccount::getAccountNumber)
                        .collect(Collectors.toSet());
        List<CreditCardAccount> subAccounts = new ArrayList<>();

        for (CreditCardAccount account : accounts) {
            Set<String> existingSubAccountNumbers =
                    subAccounts.stream()
                            .map(CreditCardAccount::getAccountNumber)
                            .collect(Collectors.toSet());

            TimelineRequest timelineRequest =
                    configuration.createTimelineRequest(
                            Integer.valueOf(account.getApiIdentifier()));
            TimelineResponse timelineResponse = apiClient.requestTimeline(timelineRequest);

            if (timelineResponse.isValidResponse()) {
                HashMap<String, String> suppIndexAccountNumberMap =
                        new HashMap<>(createSuppIndexAccountNumberMap(timelineResponse));
                completeSuppIndexAccountNumberMap.put(
                        account.getAccountNumber(), suppIndexAccountNumberMap);
                addPendingTransactionsToAccount(timelineResponse, suppIndexAccountNumberMap);
            }

            subAccounts.addAll(
                    timelineResponse.getAccounts(configuration).stream()
                            .filter(a -> !existingAccountNumbers.contains(a.getAccountNumber()))
                            .filter(a -> !existingSubAccountNumbers.contains(a.getAccountNumber()))
                            .collect(Collectors.toList()));
        }

        instanceStorage.saveCompleteSubAccountsMap(completeSuppIndexAccountNumberMap);
        return subAccounts;
    }

    /**
     * This method takes the {@link TimelineResponse} and prepares a map of suppIndex -> subCard
     * (CreditCard) accountNumber.
     *
     * @param timelineResponse
     * @return map of suppIndex -> subCard(CreditCard) accountNumber.
     */
    private Map<String, String> createSuppIndexAccountNumberMap(TimelineResponse timelineResponse) {
        return timelineResponse.getTimeline().getCardList().stream()
                .collect(
                        Collectors.toMap(
                                subCard -> subCard.getSuppIndex(),
                                subCard ->
                                        subCard.toCreditCardAccount(configuration)
                                                .getAccountNumber()));
    }

    private void addPendingTransactionsToAccount(
            TimelineResponse timelineResponse, Map<String, String> suppIndexAccountNumberMap) {
        for (String suppIndex : suppIndexAccountNumberMap.keySet()) {
            Set<TransactionEntity> accountTransactions =
                    instanceStorage.getAccountTransactions(
                            suppIndexAccountNumberMap.get(suppIndex));
            accountTransactions.addAll(timelineResponse.getPendingTransactions(suppIndex));
            instanceStorage.saveAccountTransactions(
                    suppIndexAccountNumberMap.get(suppIndex), accountTransactions);
        }
    }

    private void fetchAndStoreAllTransactions(Set<CreditCardAccount> accounts) {
        accounts.stream().forEach(account -> fetchAndSaveTransactionsForAccount(account));
    }

    /**
     * We are fetching and saving transaction during login only as transaction for one credit card
     * account can be present in the transaction response of the other credit card. So we have get
     * them all and merge them together at first step only.
     *
     * @param primaryAccount
     */
    private void fetchAndSaveTransactionsForAccount(CreditCardAccount primaryAccount) {
        int highestBillingIndex = AmericanExpressV62Constants.Fetcher.DEFAULT_MAX_BILLING_INDEX;
        int billingIndex = AmericanExpressV62Constants.Fetcher.START_BILLING_INDEX;

        int apiIdentifier = Integer.parseInt(primaryAccount.getApiIdentifier());

        boolean canFetchMore = true;
        while (billingIndex <= highestBillingIndex || canFetchMore) {
            TransactionResponse transactionResponse =
                    apiClient.requestTransaction(
                            new TransactionsRequest(ImmutableList.of(billingIndex), apiIdentifier));

            if (transactionResponse.isValidResponse()) {
                createAccountTransactionMap(transactionResponse, primaryAccount);
            }

            canFetchMore = transactionResponse.canFetchMore();
            highestBillingIndex = transactionResponse.getHighestBillingIndex();
            ++billingIndex;
        }
    }

    /**
     * This method fetches the suppIndexAccountNumberMap that belong to the primaryAccount and then
     * maps each transaction based on the suppIndex and stores it in instance storage.
     *
     * @param transactionResponse
     * @param primaryAccount - Primary CreditCardAccount for which the transactionResponse was
     *     fetched
     */
    private void createAccountTransactionMap(
            final TransactionResponse transactionResponse, final CreditCardAccount primaryAccount) {
        Map<String, String> suppIndexAccountNumberMap =
                instanceStorage.getSubAccountMap(primaryAccount.getAccountNumber());

        for (TransactionEntity entity : transactionResponse.getTransactionList()) {
            String accountForCurrentTransaction =
                    suppIndexAccountNumberMap.get(entity.getSuppIndex());

            Set<TransactionEntity> transactionSet =
                    instanceStorage.getAccountTransactions(accountForCurrentTransaction);

            transactionSet.add(entity);

            instanceStorage.saveAccountTransactions(accountForCurrentTransaction, transactionSet);
        }
    }
}
