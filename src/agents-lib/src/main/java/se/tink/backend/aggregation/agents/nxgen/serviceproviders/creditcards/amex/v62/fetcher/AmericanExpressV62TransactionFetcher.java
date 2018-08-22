package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.SubItemsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.TimelineEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TransactionsRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class AmericanExpressV62TransactionFetcher
        implements TransactionPagePaginator<CreditCardAccount> {
    private final AmericanExpressV62ApiClient client;
    private final AmericanExpressV62Configuration config;

    private AmericanExpressV62TransactionFetcher(
            AmericanExpressV62ApiClient client, AmericanExpressV62Configuration config) {
        this.client = client;
        this.config = config;
    }

    public static AmericanExpressV62TransactionFetcher create(
            AmericanExpressV62ApiClient client, AmericanExpressV62Configuration config) {
        return new AmericanExpressV62TransactionFetcher(client, config);
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        TransactionsRequest request = new TransactionsRequest();
        request.setSortedIndex(Integer.parseInt(account.getBankIdentifier()))
                .setBillingIndexList(ImmutableList.of(page));

        if (page == AmericanExpressV62Constants.Fetcher.START_PAGE) {
            return client.requestTransaction(request).getPaginatorResponse(config, fetchPendingTransactionsFor(account));
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

        List<String> pendingIdList =
                timeline.getTimelineItems()
                        .stream()
                        .map(item -> item.getSubItems().stream())
                        .flatMap(Function.identity())
                        .filter(SubItemsEntity::isPending)
                        .filter(subItem ->
                                subItem.belongToAccount(Integer.valueOf(account.getBankIdentifier()), timeline))
                        .map(SubItemsEntity::getId)
                        .collect(Collectors.toList());

        List<Transaction> pendingTransactionList = new ArrayList<>();
        for (String id : pendingIdList) {
            pendingTransactionList.add(timeline.getTransactionMap().get(id).toTransaction(config, true));
        }

        return pendingTransactionList;
    }
}
