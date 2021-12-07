package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.fetcher.account;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.NorwegianConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.client.NorwegianApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.fetcher.account.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class NorwegianTransactionFetcher<T extends Account> implements TransactionDatePaginator<T> {

    private final NorwegianApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final LocalDateTimeSource localDateTimeSource;

    @Override
    public PaginatorResponse getTransactionsFor(T account, Date fromDate, Date toDate) {
        boolean moreThan90DaysAllowed =
                canFetchMoreThan90Days(localDateTimeSource.now(ZoneId.of("UTC")));

        LocalDate currentDate = localDateTimeSource.now(ZoneId.of("UTC")).toLocalDate();

        if (!moreThan90DaysAllowed && !fetched90DaysInSession()) {
            fromDate = changeFromDateTo90DaysAgo(currentDate);
        } else if (!moreThan90DaysAllowed) {
            return PaginatorResponseImpl.createEmpty();
        }

        List<Transaction> transactions =
                fetchTransactionBatch(account.getApiIdentifier(), fromDate, toDate);
        return PaginatorResponseImpl.create(transactions);
    }

    private List<Transaction> fetchTransactionBatch(
            String accountNumber, Date fromDate, Date toDate) {
        int page = 1;
        TransactionsResponse transactionsResponse;
        List<Transaction> transactions = new LinkedList<>();
        do {
            transactionsResponse = getTransactionsPage(accountNumber, fromDate, toDate, page);
            transactions.addAll(transactionsResponse.getTinkTransactions());
            page++;
        } while (transactionsResponse.hasMorePages());
        return transactions;
    }

    private boolean canFetchMoreThan90Days(LocalDateTime now) {
        LocalDateTime consentCreationDate =
                LocalDateTime.parse(
                        persistentStorage.get(NorwegianConstants.StorageKeys.CONSENT_CREATION_DATE),
                        DateTimeFormatter.ISO_DATE_TIME);
        return ChronoUnit.MINUTES.between(consentCreationDate, now) <= 59;
    }

    private boolean fetched90DaysInSession() {
        return Boolean.TRUE.equals(
                Boolean.valueOf(
                        sessionStorage.get(
                                NorwegianConstants.StorageKeys.FETCHED_90_DAYS_OF_TRANSACTIONS)));
    }

    private Date changeFromDateTo90DaysAgo(LocalDate currentDate) {
        LocalDate fromDate = currentDate.minusDays(89);
        sessionStorage.put(NorwegianConstants.StorageKeys.FETCHED_90_DAYS_OF_TRANSACTIONS, true);

        return Date.from(fromDate.atStartOfDay().toInstant(ZoneOffset.UTC));
    }

    private TransactionsResponse getTransactionsPage(
            String accountNumber, Date fromDate, Date toDate, int page) {
        return apiClient.getTransactions(accountNumber, fromDate, toDate, page);
    }
}
