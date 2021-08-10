package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class LuminorTransactionsFetcher implements TransactionDatePaginator<TransactionalAccount> {
    private final LuminorApiClient apiClient;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        LocalDate localDateFromDate = transformToLocalDate(fromDate);
        LocalDate localDateToDate = transformToLocalDate(toDate);

        if (localDateFromDate.isBefore(get90DaysBackOnly())) {
            return PaginatorResponseImpl.createEmpty(false);
        }
        String formattedFromDate = DATE_FORMAT.format(localDateFromDate);
        String formattedToDate = DATE_FORMAT.format(localDateToDate);
        return apiClient.getTransactions(
                account.getAccountNumber(), formattedFromDate, formattedToDate);
    }

    private LocalDate get90DaysBackOnly() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime days90Before = now.minusDays(90);
        return days90Before.toLocalDate();
    }

    private LocalDate transformToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
