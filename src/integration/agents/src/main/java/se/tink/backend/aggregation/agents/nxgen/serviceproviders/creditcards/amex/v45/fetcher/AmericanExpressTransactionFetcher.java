package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc.ActivityListEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc.BillingInfoDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc.ResponseStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc.SubItemsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc.TimelineEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc.TimelineResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc.TransactionsRequest;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AmericanExpressTransactionFetcher implements TransactionFetcher<CreditCardAccount> {
    public final AggregationLogger LOGGER;
    protected final AmericanExpressApiClient apiClient;
    protected final AmericanExpressConfiguration config;

    public AmericanExpressTransactionFetcher(
            AmericanExpressApiClient apiClient, AmericanExpressConfiguration config) {
        this.apiClient = apiClient;
        this.config = config;
        LOGGER = new AggregationLogger(AmericanExpressTransactionFetcher.class);
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        List<AggregationTransaction> transactions = new ArrayList<>();
        Integer cardIndex = Integer.valueOf(account.getBankIdentifier());

        if (getPendingTransactionsWithoutError(cardIndex, transactions)) {
            getTransactions(account, transactions);
        }

        return transactions;
    }

    private boolean getPendingTransactionsWithoutError(
            Integer cardIndex, List<AggregationTransaction> transactions) {
        TimelineResponse response =
                apiClient.requestTimeline(this.config.createTimelineRequest(cardIndex));
        /*
            it is possible that amex has the expired card in the traffic.
            the card could not be identified as invalid until we require the card detail.
        */
        if (isResponseError(response.getTimeline())) {
            return false;
        }

        List<Transaction> pendingTransactions =
                getPendingTransaction(cardIndex, response.getTimeline(), this.config);

        transactions.addAll(pendingTransactions);
        return true;
    }

    private void getTransactions(
            CreditCardAccount card, List<AggregationTransaction> transactions) {

        Integer cardIndex = Integer.valueOf(card.getBankIdentifier());
        TransactionResponse response =
                apiClient.requestTransaction(createTransactionRequest(cardIndex, 0));

        if (isResponseError(response.getTransactionDetails())) {
            return;
        }
        Integer cartSupOnCurrentPage =
                response.getTransactionDetails().getCardList().stream()
                        // Comparing 4 last digits of card
                        // Temporary hack solution - agent needs rewrite for a proper one
                        .filter(
                                c ->
                                        getLast4DigitsOfTheAccountFromName(c.getCardProductName())
                                                .equals(
                                                        getLast4DigitsOfTheAccountFromName(
                                                                card.getName())))
                        .findFirst()
                        .map(c -> c.getSortedIndex())
                        .orElseGet(() -> -1);

        List<BillingInfoDetailsEntity> availableBilling;
        try {
            availableBilling =
                    response.getTransactionDetails().getBillingInfo().getBillingInfoDetails();
        } catch (NullPointerException e) {
            LOGGER.error(
                    "Can not fetch transaction for account: "
                            + SerializationUtils.serializeToString(card));
            return;
        }

        for (int billingIndex = 0; billingIndex < availableBilling.size(); billingIndex++) {

            response =
                    apiClient.requestTransaction(createTransactionRequest(cardIndex, billingIndex));
            if (isResponseError(response.getTransactionDetails())) {
                continue;
            }

            for (ActivityListEntity activityListEntity :
                    response.getTransactionDetails().getActivityList()) {
                if (activityListEntity.getTransactionList() != null) {
                    List<Transaction> transactionsFromActivity =
                            activityListEntity.getTransactionList().stream()
                                    .filter(
                                            transaction -> {
                                                try {
                                                    return Integer.valueOf(
                                                                    transaction.getSuppIndex())
                                                            .equals(cartSupOnCurrentPage);
                                                } catch (IllegalStateException e) {
                                                    LOGGER.warn(e.toString());
                                                    return false;
                                                }
                                            })
                                    .map(
                                            transaction ->
                                                    transaction.toTransaction(this.config, false))
                                    .collect(Collectors.toList());
                    transactions.addAll(transactionsFromActivity);
                }
            }
        }
    }

    private List<Transaction> getPendingTransaction(
            Integer cardIndex, TimelineEntity timeline, AmericanExpressConfiguration config) {

        List<SubItemsEntity> subItemsList =
                Optional.ofNullable(timeline.getTimelineItems()).orElseGet(Collections::emptyList)
                        .stream()
                        .map(item -> item.getSubItems().stream())
                        .flatMap(Function.identity())
                        .collect(Collectors.toList());

        List<String> pendingIdList =
                subItemsList.stream()
                        .filter(SubItemsEntity::isPending)
                        .filter(subItem -> subItem.belongToAccount(cardIndex, timeline))
                        .map(SubItemsEntity::getId)
                        .collect(Collectors.toList());
        List<Transaction> transactionList = new ArrayList<>();
        if (pendingIdList.isEmpty()) {
            return transactionList;
        }
        for (String id : pendingIdList) {
            transactionList.add(timeline.getTransactionMap().get(id).toTransaction(config, true));
        }
        return transactionList;
    }

    private TransactionsRequest createTransactionRequest(Integer cardIndex, Integer billingIndex) {
        TransactionsRequest request = new TransactionsRequest();
        request.setBillingIndexList(
                new ArrayList<>(Collections.singletonList(Integer.toString(billingIndex))));
        request.setSortedIndex(cardIndex);
        return request;
    }

    private boolean isResponseError(ResponseStatusEntity responseStatus) {
        if (responseStatus.getStatus() != 0) {
            String message = responseStatus.getMessage();
            String messageType = responseStatus.getMessageType();
            String statusCode = responseStatus.getStatusCode();

            if (statusCode != null || !message.equalsIgnoreCase("Card is cancelled")) {
                if (messageType.equalsIgnoreCase("ERROR")) {
                    LOGGER.error(
                            String.format(
                                    "Error occurred when fetching transaction: (%s) %s : %s",
                                    statusCode, messageType, message));
                } else {
                    LOGGER.warn(
                            String.format(
                                    "Something wrong when fetching transaction: (%s) %s : %s",
                                    statusCode, messageType, message));
                }
            }

            return true;
        }

        return false;
    }

    // Names of account may not be identical because of white spaces, so we use only last 4 digits
    // from the card number to compare them
    // ex. <name> - <number>, <name>-<number>
    private String getLast4DigitsOfTheAccountFromName(String name) {
        return name.split("-")[1].trim();
    }
}
