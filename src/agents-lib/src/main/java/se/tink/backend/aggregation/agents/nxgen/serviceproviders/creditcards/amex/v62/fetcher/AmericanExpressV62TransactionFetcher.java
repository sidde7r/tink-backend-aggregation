package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Predicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.SubItemsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.SubcardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.TimelineEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TransactionsRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class AmericanExpressV62TransactionFetcher
        implements TransactionPagePaginator<CreditCardAccount> {
    private final AmericanExpressV62ApiClient client;
    private final AmericanExpressV62Configuration config;
    private final SessionStorage sessionStorage;

    private AmericanExpressV62TransactionFetcher(
            AmericanExpressV62ApiClient client,
            AmericanExpressV62Configuration config,
            SessionStorage sessionStorage) {
        this.client = client;
        this.config = config;
        this.sessionStorage = sessionStorage;
    }

    public static AmericanExpressV62TransactionFetcher create(
            AmericanExpressV62ApiClient client,
            AmericanExpressV62Configuration config,
            SessionStorage sessionStorage) {
        return new AmericanExpressV62TransactionFetcher(client, config, sessionStorage);
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        TransactionsRequest request = new TransactionsRequest();
        request.setSortedIndex(Integer.parseInt(account.getBankIdentifier()))
                .setBillingIndexList(ImmutableList.of(page));

        if (page == AmericanExpressV62Constants.Fetcher.START_PAGE) {
            return client.requestTransaction(request)
                    .getPaginatorResponse(config, fetchPendingTransactionsFor(account));
        }

        return client.requestTransaction(request).getPaginatorResponse(config);
    }

    private List<Transaction> fetchPendingTransactionsFor(CreditCardAccount account) {
        TimelineRequest timelineRequest =
                config.createTimelineRequest(Integer.valueOf(account.getBankIdentifier()));
        TimelineResponse timelineResponse = client.requestTimeline(timelineRequest);

        return getPendingTransactionsFor(account, timelineResponse.getTimeline());
    }

    private List<Transaction> getPendingTransactionsFor(
            CreditCardAccount account, TimelineEntity timeline) {
        List<CardEntity> partnerCardEntities =
                AmericanExpressV62Predicates.filterOutMainCardFromPartnerCards(
                        sessionStorage
                                .get(
                                        AmericanExpressV62Constants.Tags.CARD_LIST,
                                        new TypeReference<List<CardEntity>>() {})
                                .orElse(Collections.emptyList()),
                        account);

        List<SubcardEntity> partnerCards =
                AmericanExpressV62Predicates.getPartnerCardsFromSubcards(
                        timeline.getCardList(), partnerCardEntities);

        List<String> pendingIdList =
                Optional.ofNullable(timeline.getTimelineItems())
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .map(item -> item.getSubItems().stream())
                        .flatMap(Function.identity())
                        .filter(SubItemsEntity::isPending)
                        .map(SubItemsEntity::getId)
                        .collect(Collectors.toList());

        List<Transaction> pendingTransactionList = new ArrayList<>();
        pendingTransactionList.addAll(
                pendingIdList
                        .stream()
                        .map(id -> timeline.getTransactionMap().get(id))
                        .filter(
                                transaction ->
                                        AmericanExpressV62Predicates.filterPartnerTransactions.test(
                                                transaction, partnerCards))
                        .map(t -> t.toTransaction(config, true))
                        .collect(Collectors.toList()));

        return pendingTransactionList;
    }
}
