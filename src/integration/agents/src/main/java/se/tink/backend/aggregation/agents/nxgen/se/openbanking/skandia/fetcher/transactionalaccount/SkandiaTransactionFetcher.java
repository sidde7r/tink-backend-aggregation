package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@AllArgsConstructor
public class SkandiaTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private static final int MAXIMUM_YEARS_TO_FETCH = 2;
    private SkandiaApiClient apiClient;
    private LocalDateTimeSource localDateTimeSource;
    private TransactionPaginationHelper paginationHelper;

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        final URL transactionsUrl;
        LocalDate currentDate = localDateTimeSource.now().toLocalDate();

        if (key != null) {
            transactionsUrl = new URL(Urls.BASE_URL.concat(key));
            return apiClient.getTransactions(transactionsUrl);
        } else {
            final LocalDate fromDate;
            Optional<Date> certainDate = paginationHelper.getTransactionDateLimit(account);
            if (!certainDate.isPresent()) {
                fromDate = currentDate.minusYears(MAXIMUM_YEARS_TO_FETCH);
            } else {
                fromDate =
                        certainDate.get().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }

            URL transactionsUrlPending =
                    Urls.GET_TRANSACTIONS
                            .parameter(IdTags.ACCOUNT_ID, account.getApiIdentifier())
                            .queryParam(QueryKeys.DATE_FROM, currentDate.toString())
                            .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.PENDING);

            URL transactionsUrlBooked =
                    Urls.GET_TRANSACTIONS
                            .parameter(IdTags.ACCOUNT_ID, account.getApiIdentifier())
                            .queryParam(QueryKeys.DATE_FROM, fromDate.toString())
                            .queryParam(QueryKeys.DATE_TO, currentDate.toString())
                            .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED);

            GetTransactionsResponse pendingTransactions =
                    apiClient.getTransactions(transactionsUrlPending);
            GetTransactionsResponse transactions = apiClient.getTransactions(transactionsUrlBooked);
            transactions.setPending(pendingTransactions.getPending());
            return transactions;
        }
    }
}
