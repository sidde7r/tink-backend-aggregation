package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.fetcher;

import java.time.LocalDate;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.AbnAmroApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.AbnAmroUserIpInformation;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class AbnAmroTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private static final int MAX_MONTHS_TO_FETCH = 18;
    private static final int MAX_DAYS_TO_FETCH_FOR_BG_REFRESH = 89;

    private final AbnAmroApiClient apiClient;
    private final AbnAmroUserIpInformation userIpInformation;

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        String accountId = account.getAccountNumber();
        if (isBackgroundRefresh()) {
            return fetchBackgroundRefreshTransactions(accountId);
        } else {
            return fetchManualRefreshTransactions(key, accountId);
        }
    }

    private boolean isBackgroundRefresh() {
        return !userIpInformation.isManualRequest();
    }

    private TransactionKeyPaginatorResponse<String> fetchBackgroundRefreshTransactions(
            String accountId) {
        LocalDate now = LocalDate.now();
        LocalDate minDate = now.minusDays(MAX_DAYS_TO_FETCH_FOR_BG_REFRESH);
        TransactionsResponse transactionsResponse =
                apiClient.fetchTransactionsByDate(accountId, minDate, now);
        return new TransactionKeyPaginatorResponseImpl<>(
                transactionsResponse.getTinkTransactions(), null);
    }

    private TransactionKeyPaginatorResponse<String> fetchManualRefreshTransactions(
            String key, String accountId) {
        if (isFirstTransactionRequest(key)) {
            LocalDate now = LocalDate.now();
            LocalDate minDate = now.minusMonths(MAX_MONTHS_TO_FETCH);
            return apiClient.fetchTransactionsByDate(accountId, minDate, now);
        }
        return apiClient.fetchTransactionsByKey(key, accountId);
    }

    private boolean isFirstTransactionRequest(String key) {
        return Objects.isNull(key);
    }
}
