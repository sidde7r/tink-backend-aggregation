package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@RequiredArgsConstructor
@Slf4j
public class JyskeBankTransactionFetcher implements TransactionPagePaginator<TransactionalAccount> {
    private final JyskeBankApiClient apiClient;
    private static final int MAX_CONCURRENCY = 4;
    private static final int PRE_FETCH = 1;
    private static final int PAGES_PER_BATCH = 20;

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        final String accountId = account.getFromTemporaryStorage(Storage.PUBLIC_ID);
        final int firstPage = page * PAGES_PER_BATCH;

        final List<TransactionResponse> transactionResponse =
                fetchBatchOfPages(accountId, firstPage, PAGES_PER_BATCH);

        final Collection<? extends Transaction> transactions =
                transactionResponse.stream()
                        .map(TransactionResponse::getTransactions)
                        .flatMap(Collection::stream)
                        .map(TransactionsEntity::toTinkTransaction)
                        .collect(Collectors.toList());

        final boolean canFetchMore =
                transactionResponse.stream()
                        .map(TransactionResponse::isHasMoreTransactions)
                        .reduce(Boolean::logicalAnd)
                        .orElse(false);

        return PaginatorResponseImpl.create(transactions, canFetchMore);
    }

    private List<TransactionResponse> fetchBatchOfPages(
            String accountId, int firstPage, int numberOfPages) {
        return Observable.range(firstPage, numberOfPages)
                .concatMapEager(x -> fetchSinglePage(accountId, x), MAX_CONCURRENCY, PRE_FETCH)
                .toList()
                .blockingGet();
    }

    private Observable<TransactionResponse> fetchSinglePage(String accountId, int pageNumber) {
        return Observable.fromCallable(() -> apiClient.fetchTransactions(accountId, pageNumber))
                .subscribeOn(Schedulers.io());
    }
}
