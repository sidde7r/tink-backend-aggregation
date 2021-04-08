package se.tink.backend.aggregation.agents.nxgen.de.openbanking.comdirect;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.ErrorEntity.CONSENT_INVALID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.ErrorEntity.CONSENT_TIME_OUT_EXPIRED;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
@Slf4j
public class ParallelTransactionFetcher<A extends Account> implements TransactionFetcher<A> {

    private final Xs2aDevelopersApiClient apiClient;
    private final LocalDateTimeSource localDateTimeSource;

    private static final int MAX_CONCURRENCY = 4;
    private static final int SECOND_PAGE_NUMBER = 1;

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(A account) {

        // Fetch first page, either from all history, or last 90 days
        GetTransactionsResponse firstPage = fetchFirstPage(account);

        // check how many pages we deal with
        String linkToLastPage = firstPage.getTransactions().getLinks().getLast().get();
        int indexOfLastEquals = linkToLastPage.lastIndexOf('=');
        String linkWithoutPageNumber = linkToLastPage.substring(0, indexOfLastEquals + 1);
        int lastPageNumber = Integer.parseInt(linkToLastPage.substring(indexOfLastEquals + 1));

        List<GetTransactionsResponse> pages = new ArrayList<>();
        pages.add(firstPage);
        if (lastPageNumber > 0) {
            if (lastPageNumber < MAX_CONCURRENCY) {
                pages.addAll(fetchInSequence(linkWithoutPageNumber, lastPageNumber));
            } else {
                log.info("Started fetching [" + lastPageNumber + "] pages in parallel.");
                pages.addAll(fetchInParallel(linkWithoutPageNumber, lastPageNumber));
                log.info("Finished fetching [" + lastPageNumber + "] pages in parallel.");
            }
        }

        return pages.stream()
                .flatMap(x -> x.getTinkTransactions().stream())
                .collect(Collectors.toList());
    }

    private GetTransactionsResponse fetchFirstPage(A account) {
        LocalDate today = localDateTimeSource.now().toLocalDate();
        try {
            return apiClient.getTransactions(account, LocalDate.ofEpochDay(0), today);
        } catch (HttpResponseException hre) {
            if (isConsentTimeoutException(hre)) {
                return apiClient.getTransactions(account, today.minusDays(89), today);
            } else {
                throw hre;
            }
        }
    }

    private boolean isConsentTimeoutException(HttpResponseException ex) {
        ErrorResponse errorResponse = ex.getResponse().getBody(ErrorResponse.class);
        if (errorResponse == null || errorResponse.getTppMessages() == null) {
            return false;
        }
        return errorResponse.getTppMessages().stream()
                .anyMatch(x -> CONSENT_INVALID.equals(x) || CONSENT_TIME_OUT_EXPIRED.equals(x));
    }

    private List<GetTransactionsResponse> fetchInSequence(
            String linkWithoutPageNumber, int lastPageNumber) {
        return IntStream.rangeClosed(SECOND_PAGE_NUMBER, lastPageNumber)
                .mapToObj(x -> apiClient.getTransactions(linkWithoutPageNumber + x))
                .collect(Collectors.toList());
    }

    private List<GetTransactionsResponse> fetchInParallel(
            String linkWithoutPageNumber, int lastPageNumber) {
        return Observable.range(SECOND_PAGE_NUMBER, lastPageNumber)
                .concatMapEager(x -> fetchSingle(linkWithoutPageNumber, x), MAX_CONCURRENCY, 1)
                .toList()
                .blockingGet();
    }

    private Observable<GetTransactionsResponse> fetchSingle(
            String linkWithoutPageNumber, int pageNumber) {
        return Observable.fromCallable(
                        () -> apiClient.getTransactions(linkWithoutPageNumber + pageNumber))
                .subscribeOn(Schedulers.io());
    }
}
