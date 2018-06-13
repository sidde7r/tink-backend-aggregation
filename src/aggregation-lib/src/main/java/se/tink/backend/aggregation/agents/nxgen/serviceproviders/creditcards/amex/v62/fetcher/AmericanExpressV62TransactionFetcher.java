package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.PendingTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.SubItemsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.TimelineEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TransactionsRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class AmericanExpressV62TransactionFetcher
        implements TransactionPagePaginator<CreditCardAccount>,
        UpcomingTransactionFetcher<CreditCardAccount> {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(AmericanExpressV62TransactionFetcher.class);
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
    public TransactionPagePaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        TransactionsRequest request = new TransactionsRequest();
        request
                .setSortedIndex(Integer.parseInt(account.getBankIdentifier()))
                .setBillingIndexList(ImmutableList.of(page));

        TransactionPagePaginatorResponse transactionPagePaginatorResponse =
                client.requestTransaction(request).setConfig(config).getPaginatorResponse();
        return transactionPagePaginatorResponse;
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(CreditCardAccount account) {
        TimelineRequest timelineRequest =
                config.createTimelineRequest(Integer.valueOf(account.getBankIdentifier()));
        TimelineResponse timelineResponse = client.requestTimeline(timelineRequest);
        List<PendingTransactionEntity> pendingTransactions =
                getPendingTransactionsFor(account, timelineResponse.getTimeline());
        return pendingTransactions
                .stream()
                .map(t -> t.toUpcomingTransaction(config))
                .collect(Collectors.toList());
    }

    private List<PendingTransactionEntity> getPendingTransactionsFor(
            CreditCardAccount account, TimelineEntity timeline) {

        List<String> pendingIdList =
                timeline
                        .getTimelineItems()
                        .stream()
                        .map(item -> item.getSubItems().stream())
                        .flatMap(Function.identity())
                        .filter(SubItemsEntity::isPending)
                        .filter(
                                subItem ->
                                        subItem.belongToAccount(Integer.valueOf(account.getBankIdentifier()), timeline))
                        .map(SubItemsEntity::getId)
                        .collect(Collectors.toList());

        List<PendingTransactionEntity> transactionList = new ArrayList<>();
        for (String id : pendingIdList) {
            transactionList.add((PendingTransactionEntity) timeline.getTransactionMap().get(id));
        }

        return transactionList;
    }
}
