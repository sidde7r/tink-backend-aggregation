package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.rpc.FetchErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersForAgentPlatformApiClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.KeyWithInitiDateFromFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
@Slf4j
public class N26DevelopersTransactionDateFromFetcher<A extends Account>
        implements KeyWithInitiDateFromFetcher<A, String> {

    private static final LocalDate START_DATE_89_DAYS = LocalDate.now().minusDays(89);
    private static final LocalDate START_DATE_ALL_HISTORY = LocalDate.ofEpochDay(0);

    private final Xs2aDevelopersForAgentPlatformApiClient apiClient;
    private final LocalDateTimeSource localDateTimeSource;
    private final boolean isManual;

    @Override
    public LocalDate minimalFromDate() {
        return START_DATE_ALL_HISTORY;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(A account, String key) {
        return apiClient.getTransactions(key);
    }

    @Override
    public TransactionKeyPaginatorResponse<String> fetchTransactionsFor(
            A account, LocalDate dateFrom) {
        if (isManual) {
            return fetchAllTransactionsWithFallback(account, dateFrom);
        } else {
            return fetchTransactionsForLast89Days(account);
        }
    }

    private TransactionKeyPaginatorResponse<String> fetchAllTransactionsWithFallback(
            A account, LocalDate dateFrom) {
        try {
            return apiClient.getTransactions(
                    account, dateFrom, localDateTimeSource.now().toLocalDate());
        } catch (HttpResponseException hre) {
            if (isConsentTimeoutException(hre)) {
                return fetchTransactionsForLast89Days(account);
            } else {
                throw hre;
            }
        }
    }

    private TransactionKeyPaginatorResponse<String> fetchTransactionsForLast89Days(A account) {
        return apiClient.getTransactions(
                account, START_DATE_89_DAYS, localDateTimeSource.now().toLocalDate());
    }

    private boolean isConsentTimeoutException(HttpResponseException ex) {
        FetchErrorResponse errorResponse = ex.getResponse().getBody(FetchErrorResponse.class);
        if (errorResponse == null || errorResponse.getCode() == null) {
            return false;
        }
        return ErrorCodes.PERIOD_INVALID.equals(errorResponse.getCode());
    }
}
