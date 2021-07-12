package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class LuminorTransactionsFetcher implements TransactionDatePaginator<TransactionalAccount> {
    private final LuminorApiClient apiClient;

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        Date today90 = get90DaysBackOnly();

        if (fromDate.before(today90)) {
            return PaginatorResponseImpl.createEmpty(false);
        }
        String formattedFromDate = formatDate(fromDate);
        String formattedToDate = formatDate(toDate);
        return apiClient.getTransactions(
                account.getAccountNumber(), formattedFromDate, formattedToDate);
    }

    private Date get90DaysBackOnly() {
        Date today = new Date();
        Calendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, -90);
        return cal.getTime();
    }

    private String formatDate(Date date) {
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return dateFormatter.format(date);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
