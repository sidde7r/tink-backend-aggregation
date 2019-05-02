package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.Fetcher.START_BILLING_INDEX;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.Storage.TRANSACTIONS;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TransactionsRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class AmericanExpressV62CreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final SessionStorage sessionStorage;
    private final AmericanExpressV62Configuration configuration;
    private final AmericanExpressV62ApiClient apiClient;

    private AmericanExpressV62CreditCardFetcher(
            SessionStorage sessionStorage,
            AmericanExpressV62Configuration configuration,
            final AmericanExpressV62ApiClient apiClient) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.configuration = configuration;
    }

    public static AmericanExpressV62CreditCardFetcher create(
            SessionStorage sessionStorage,
            AmericanExpressV62Configuration config,
            AmericanExpressV62ApiClient apiClient) {
        return new AmericanExpressV62CreditCardFetcher(sessionStorage, config, apiClient);
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<CardEntity> cardEntities =
                sessionStorage
                        .get(
                                AmericanExpressV62Constants.Tags.CARD_LIST,
                                new TypeReference<List<CardEntity>>() {})
                        .orElse(Collections.emptyList());
        Collection<CreditCardAccount> accounts =
                cardEntities.stream()
                        .map(card -> card.toCreditCardAccount(configuration))
                        .collect(Collectors.toList());
        accounts.addAll(fetchSubAccountsFromTimeline(accounts));
        // Fetch all transactions we can find, because they seem to not be card specific
        // so we fetch as much as we can and store them to be filtered through later.
        fetchAndStoreAllTransactions(accounts);
        return accounts;
    }

    private void fetchAndStoreAllTransactions(Collection<CreditCardAccount> accounts) {
        int billingIndex = START_BILLING_INDEX;
        Set<TransactionResponse> transactions = new HashSet<>();
        for (CreditCardAccount account : accounts) {

            // Sub accounts do not have an api identifier.
            if (Strings.isNullOrEmpty(account.getApiIdentifier())) {
                continue;
            }
            boolean canFetchMore = true;
            while (canFetchMore) {
                TransactionsRequest request = new TransactionsRequest();
                request.setSortedIndex(Integer.parseInt(account.getApiIdentifier()))
                        .setBillingIndexList(ImmutableList.of(billingIndex));
                TransactionResponse resp = apiClient.requestTransaction(request);
                if (resp.isOkResponse()) {
                    transactions.add(resp);
                }
                canFetchMore = resp.canFetchMore();
                ++billingIndex;
            }
        }

        sessionStorage.put(TRANSACTIONS, transactions);
    }

    private Collection<? extends CreditCardAccount> fetchSubAccountsFromTimeline(
            final Collection<CreditCardAccount> accounts) {

        Set<String> existingAccountNumbers =
                accounts.stream()
                        .map(CreditCardAccount::getAccountNumber)
                        .collect(Collectors.toSet());
        List<CreditCardAccount> subAccounts = new ArrayList<>();

        Set<TimelineResponse> timeLines = new HashSet<>();
        for (CreditCardAccount account : accounts) {
            Set<String> existingSubAccountNumbers =
                    subAccounts.stream()
                            .map(CreditCardAccount::getAccountNumber)
                            .collect(Collectors.toSet());

            TimelineRequest timelineRequest =
                    configuration.createTimelineRequest(
                            Integer.valueOf(account.getApiIdentifier()));
            TimelineResponse resp = apiClient.requestTimeline(timelineRequest);
            timeLines.add(resp);

            subAccounts.addAll(
                    resp.getAccounts(configuration).stream()
                            .filter(a -> !existingAccountNumbers.contains(a.getAccountNumber()))
                            .filter(a -> !existingSubAccountNumbers.contains(a.getAccountNumber()))
                            .collect(Collectors.toList()));
        }
        sessionStorage.put(AmericanExpressV62Constants.Storage.TIME_LINES, timeLines);
        return subAccounts;
    }
}
