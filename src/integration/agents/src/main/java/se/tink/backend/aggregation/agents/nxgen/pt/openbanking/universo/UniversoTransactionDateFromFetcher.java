package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo;

import java.time.LocalDate;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionDateFromFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class UniversoTransactionDateFromFetcher
        extends Xs2aDevelopersTransactionDateFromFetcher<TransactionalAccount> {

    private final String baseUrl;

    public UniversoTransactionDateFromFetcher(
            Xs2aDevelopersApiClient apiClient,
            LocalDateTimeSource localDateTimeSource,
            boolean userPresent,
            String baseUrl) {
        super(apiClient, localDateTimeSource, userPresent);
        this.baseUrl = baseUrl;
    }

    @Override
    public LocalDate minimalFromDate() {
        return LocalDate.now().minusYears(10).plusDays(1);
    }

    /**
     * Universo returned transactions next value (e.g.
     * next":{"href":"/v1/accounts/accountId/transactions?dateTo=2021-06-29&bookingStatus=booked&dateFrom=2011-06-30&range=50-99")
     * may not have any values and 404 status is returned. In that case empty response has to be
     * returned to the controller to stop the flow.
     */
    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        try {
            return super.getTransactionsFor(account, baseUrl + key);
        } catch (HttpResponseException ex) {
            if (ex.getResponse().getStatus() == 404) {
                return getEmptyResponse();
            }
            throw ex;
        }
    }

    private TransactionKeyPaginatorResponse<String> getEmptyResponse() {
        return new TransactionKeyPaginatorResponseImpl<>(Collections.emptyList(), null);
    }
}
