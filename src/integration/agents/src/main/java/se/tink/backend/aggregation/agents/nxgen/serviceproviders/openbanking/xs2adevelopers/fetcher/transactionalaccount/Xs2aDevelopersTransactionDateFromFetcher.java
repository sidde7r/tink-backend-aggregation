package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.ErrorEntity.CONSENT_INVALID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.ErrorEntity.CONSENT_TIME_OUT_EXPIRED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.ErrorEntity.SERVICE_UNAVAILABLE;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.KeyWithInitiDateFromFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@AllArgsConstructor
public class Xs2aDevelopersTransactionDateFromFetcher<A extends Account>
        implements KeyWithInitiDateFromFetcher<A, String> {

    private static final LocalDate START_DATE_89_DAYS = LocalDate.now().minusDays(89);
    private static final LocalDate START_DATE_ALL_HISTORY = LocalDate.ofEpochDay(0);

    private final Xs2aDevelopersApiClient apiClient;
    protected final LocalDateTimeSource localDateTimeSource;
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
            if (isConsentTimeoutException(hre) || is503ServiceUnavailable(hre)) {
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
        ErrorResponse errorResponse = ex.getResponse().getBody(ErrorResponse.class);
        if (errorResponse == null || errorResponse.getTppMessages() == null) {
            return false;
        }
        return errorResponse.getTppMessages().stream()
                .anyMatch(x -> CONSENT_INVALID.equals(x) || CONSENT_TIME_OUT_EXPIRED.equals(x));
    }

    // ITE-2400
    // Fix for comdirect API issues, they seem to hit 503 whenever we ask for long periods of time
    // Written in a roundabout fashion of catching exception by design!
    // This will allow us to fetch whole history again immediatly after they fix their shit.
    private boolean is503ServiceUnavailable(HttpResponseException ex) {
        ErrorResponse errorResponse = ex.getResponse().getBody(ErrorResponse.class);
        return ex.getResponse().getStatus() == 503
                && errorResponse != null
                && errorResponse.getTppMessages() != null
                && errorResponse.getTppMessages().stream().anyMatch(SERVICE_UNAVAILABLE::equals);
    }
}
