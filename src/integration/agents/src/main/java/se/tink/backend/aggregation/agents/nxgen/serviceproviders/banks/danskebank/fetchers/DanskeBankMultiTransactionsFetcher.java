package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.FirstPartitionTransactionPage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.FutureTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.FutureTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
public class DanskeBankMultiTransactionsFetcher<A extends Account>
        implements TransactionDatePaginator<A>, UpcomingTransactionFetcher<A> {

    private static final ZoneId TIMEZONE = TimeZone.getTimeZone("Europe/Stockholm").toZoneId();
    private static final int PARTITION_COUNT = 4;
    private static final long NUM_DAYS_IN_PARTITION = 90;

    private final DanskeBankApiClient apiClient;
    private final String languageCode;

    @Override
    public PaginatorResponse getTransactionsFor(A account, Date fromDate, Date toDate) {
        LocalDate to = toDate.toInstant().atZone(TIMEZONE).toLocalDate();

        List<Transaction> transactions =
                Observable.range(0, PARTITION_COUNT)
                        .map(partitionNumber -> calculatePartitionDates(partitionNumber, to))
                        .concatMapEager(
                                partitionPeriod ->
                                        fetchFirstTransactionsPage(account, partitionPeriod))
                        .flatMapSingle(this::collectTransactions)
                        .flatMapIterable(transactionEntities -> transactionEntities)
                        .map(TransactionEntity::toTinkTransaction)
                        .toList()
                        .blockingGet();

        return PaginatorResponseImpl.create(transactions);
    }

    private Pair<String, String> calculatePartitionDates(Integer partitionNumber, LocalDate now) {
        LocalDate partitionDateTo = now.minusDays(partitionNumber * NUM_DAYS_IN_PARTITION);
        LocalDate partitionDateFrom = partitionDateTo.minusDays(NUM_DAYS_IN_PARTITION - 1);

        String dateFrom = partitionDateFrom.format(DateTimeFormatter.BASIC_ISO_DATE);
        String dateTo = partitionDateTo.format(DateTimeFormatter.BASIC_ISO_DATE);
        return Pair.of(dateFrom, dateTo);
    }

    private Observable<FirstPartitionTransactionPage> fetchFirstTransactionsPage(
            A account, Pair<String, String> partitionPeriod) {
        ListTransactionsRequest listTransactionsRequest =
                ListTransactionsRequest.create(
                        this.languageCode,
                        account.getApiIdentifier(),
                        partitionPeriod.getLeft(),
                        partitionPeriod.getRight());

        return Observable.fromCallable(() -> safeCallForTransactions(listTransactionsRequest))
                .subscribeOn(Schedulers.io())
                .map(
                        transactionsResponse ->
                                new FirstPartitionTransactionPage(
                                        listTransactionsRequest,
                                        transactionsResponse,
                                        partitionPeriod));
    }

    private Single<List<TransactionEntity>> collectTransactions(
            FirstPartitionTransactionPage firstPartitionPage) {

        List<TransactionEntity> result =
                firstPartitionPage.getTransactionsResponse().getTransactions();

        return Single.fromCallable(() -> fetchTransactionsContinuation(firstPartitionPage))
                .subscribeOn(Schedulers.io())
                .map(
                        transactionEntityList -> {
                            transactionEntityList.addAll(result);
                            return transactionEntityList;
                        });
    }

    private List<TransactionEntity> fetchTransactionsContinuation(
            FirstPartitionTransactionPage firstPartitionTransactionPage) {
        List<TransactionEntity> transactions = new ArrayList<>();
        ListTransactionsResponse transactionsResponse =
                firstPartitionTransactionPage.getTransactionsResponse();
        ListTransactionsRequest transactionsRequest =
                firstPartitionTransactionPage.getTransactionsRequest();
        while (!transactionsResponse.isEndOfList()) {
            transactionsRequest.setRepositionKey(transactionsResponse.getRepositionKey());
            transactionsResponse = safeCallForTransactions(transactionsRequest);

            transactions.addAll(transactionsResponse.getTransactions());
        }

        return transactions;
    }

    private ListTransactionsResponse safeCallForTransactions(
            ListTransactionsRequest transactionsRequest) {
        try {
            return apiClient.listTransactions(transactionsRequest);
        } catch (HttpResponseException e) {
            try {
                // If we are able to deserialize the response we can be certain that we have made a
                // successful request but Danske Bank limits how far back we can fetch.
                ListTransactionsResponse body =
                        e.getResponse().getBody(ListTransactionsResponse.class);
                body.setEndOfList(true);
                return body;
            } catch (RuntimeException e1) {
                throw new HttpResponseException(e.getRequest(), e.getResponse());
            }
        }
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(A account) {
        FutureTransactionsResponse futureTransactionsResponse =
                this.apiClient.listUpcomingTransactions(
                        FutureTransactionsRequest.create(
                                this.languageCode, account.getApiIdentifier()));

        return futureTransactionsResponse.getTransactions().stream()
                .map(TransactionEntity::toTinkUpcomingTransaction)
                .collect(Collectors.toList());
    }
}
