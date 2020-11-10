package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbExceptionsHelper;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
public class DnbTransactionFetcher implements TransactionPaginator<TransactionalAccount> {

    private static final int BAD_REQUEST_CODE = 400;
    private static final long DELAY_MILLISECONDS = 400;
    private static final int BAD_REQUEST_MAX_TRY_COUNTER = 10;
    private static final int DEFAULT_COUNT_INCREMENT = 50;
    // Tested that 999 is the maximal supported count for DNB
    private static final int MAXIMUM_TRANSACTIONS_TO_FETCH = 999;

    private final DnbApiClient apiClient;

    private boolean canFetchMore = false;
    private int transactionsToFetch;
    private int badRequestCounter = 0;

    @Override
    public void resetState() {
        transactionsToFetch = DEFAULT_COUNT_INCREMENT;
        canFetchMore = false;
        badRequestCounter = 0;
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(TransactionalAccount account) {
        TransactionResponse transactionResponse;
        List<TransactionEntity> transactionEntityList;
        try {
            HttpResponse httpResponse = apiClient.fetchTransactions(account, transactionsToFetch);
            // In practice, it will eventually return 400 Bad Request when fetching around 20 times
            // in a roll.
            // By adding a delay of 400 ms solves the issue. This delay is tested with 2000 times of
            // fetch.
            Uninterruptibles.sleepUninterruptibly(DELAY_MILLISECONDS, TimeUnit.MILLISECONDS);

            transactionResponse = httpResponse.getBody(TransactionResponse.class);
            transactionEntityList = transactionResponse.getTransactionList();
            if (transactionEntityList.isEmpty()) {
                resetState();
                return PaginatorResponseImpl.createEmpty(canFetchMore);
            }
            List<Transaction> transactions =
                    transactionEntityList
                            .subList(
                                    transactionsToFetch - DEFAULT_COUNT_INCREMENT,
                                    Math.min(transactionsToFetch, transactionEntityList.size()))
                            .stream()
                            .map(TransactionEntity::toTinkTransaction)
                            .collect(Collectors.toList());

            canFetchMore =
                    transactionResponse.getPosition() != null
                            && transactionsToFetch != MAXIMUM_TRANSACTIONS_TO_FETCH;

            return PaginatorResponseImpl.create(transactions, canFetchMore);
        } catch (HttpResponseException e) {
            if (DnbExceptionsHelper.customerDoesNotHaveAccessToResource(e)) {
                return PaginatorResponseImpl.create(Collections.emptyList(), false);
            } else if (e.getResponse().getStatus() != BAD_REQUEST_CODE) {
                resetState();
                throw e;
            } else {
                if (badRequestCounter >= BAD_REQUEST_MAX_TRY_COUNTER) {
                    resetState();
                    throw new IllegalStateException(
                            "Fetch Transaction caused bad request for more than max trials", e);
                }
                // Preventive & experimental code, check if dnb will recover a 400 if sleep a while
                badRequestCounter += 1;
                Uninterruptibles.sleepUninterruptibly(
                        DELAY_MILLISECONDS + (badRequestCounter * DELAY_MILLISECONDS),
                        TimeUnit.MILLISECONDS);
                transactionsToFetch -= DEFAULT_COUNT_INCREMENT;
                return PaginatorResponseImpl.createEmpty(canFetchMore);
            }
        } finally {
            increaseTransactionsToFetch();
        }
    }

    private void increaseTransactionsToFetch() {
        if (canFetchMore) {
            transactionsToFetch += DEFAULT_COUNT_INCREMENT;
            if (transactionsToFetch > MAXIMUM_TRANSACTIONS_TO_FETCH) {
                transactionsToFetch = MAXIMUM_TRANSACTIONS_TO_FETCH;
            }
        } else {
            transactionsToFetch = DEFAULT_COUNT_INCREMENT;
        }
    }
}
