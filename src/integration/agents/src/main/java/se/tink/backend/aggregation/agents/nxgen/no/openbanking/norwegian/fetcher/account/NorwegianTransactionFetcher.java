package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.NorwegianConstants;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.client.NorwegianApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NorwegianTransactionFetcher<T extends Account> implements TransactionDatePaginator<T> {

    private final NorwegianApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    public NorwegianTransactionFetcher(
            NorwegianApiClient apiClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        this.apiClient = Objects.requireNonNull(apiClient);
        this.persistentStorage = Objects.requireNonNull(persistentStorage);
        this.sessionStorage = Objects.requireNonNull(sessionStorage);
    }

    @Override
    public PaginatorResponse getTransactionsFor(T account, Date fromDate, Date toDate) {
        boolean moreThan90DaysAllowed = canFetchMoreThan90Days();

        if (!moreThan90DaysAllowed && !fetched90DaysInSession()) {
            fromDate = changeFromDateTo90DaysAgo();
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

    private boolean canFetchMoreThan90Days() {
        LocalDateTime consentCreationDate =
                LocalDateTime.parse(
                        persistentStorage.get(NorwegianConstants.StorageKeys.CONSENT_CREATION_DATE),
                        DateTimeFormatter.ISO_DATE_TIME);
        return ChronoUnit.MINUTES.between(consentCreationDate, LocalDateTime.now()) <= 59;
    }

    private boolean fetched90DaysInSession() {
        return Boolean.TRUE.equals(
                Boolean.valueOf(
                        sessionStorage.get(
                                NorwegianConstants.StorageKeys.FETCHED_90_DAYS_OF_TRANSACTIONS)));
    }

    private Date changeFromDateTo90DaysAgo() {
        Date fromDate =
                Date.from(
                        LocalDateTime.now()
                                .minus(89, ChronoUnit.DAYS)
                                .atZone(ZoneId.systemDefault())
                                .toInstant());
        sessionStorage.put(NorwegianConstants.StorageKeys.FETCHED_90_DAYS_OF_TRANSACTIONS, true);
        return fromDate;
    }

    private TransactionsResponse getTransactionsPage(
            String accountNumber, Date fromDate, Date toDate, int page) {
        return apiClient.getTransactions(accountNumber, fromDate, toDate, page);
    }
}
