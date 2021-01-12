package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
@Slf4j
public class CreditAgricoleBaseTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private static final long MAX_NUM_MONTHS_FOR_FETCH = 13L;

    private final CreditAgricoleBaseApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final Clock clock;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        GetAccountsResponse getAccountsResponse = apiClient.getAccounts();

        if (getAccountsResponse.getAccounts().stream()
                .filter(acc -> !acc.getCashAccountType().equals("CACC"))
                .findFirst()
                .isPresent()) {
            log.info("Account type different then CACC.");
        }

        if (getAccountsResponse.areConsentsNecessary()) {
            apiClient.putConsents(getAccountsResponse.getListOfNecessaryConsents());
            getAccountsResponse = apiClient.getAccounts();
        }

        return getAccountsResponse.toTinkAccounts();
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        final LocalDate fromDateLocal = getLocalDateFromDate(fromDate);
        final LocalDate toDateLocal = getLocalDateFromDate(toDate);

        return isInitialFetch()
                ? getAllTransactions(account, fromDateLocal, toDateLocal)
                : get90DaysTransactions(account, toDateLocal);
    }

    private PaginatorResponse getAllTransactions(
            TransactionalAccount account, LocalDate fromDate, LocalDate toDate) {

        final LocalDate oldestDateForFetch = getOldestDateForTransactionFetch();

        if (oldestDateForFetch.isAfter(toDate)) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        final LocalDate limitedFromDate =
                oldestDateForFetch.isAfter(fromDate) ? oldestDateForFetch : fromDate;

        return PaginatorResponseImpl.create(
                apiClient
                        .getTransactions(account.getApiIdentifier(), limitedFromDate, toDate)
                        .getTinkTransactions());
    }

    private PaginatorResponse get90DaysTransactions(
            TransactionalAccount account, LocalDate toDate) {
        final LocalDate fromDate = LocalDate.now(clock).minusDays(89L);
        return PaginatorResponseImpl.create(
                apiClient
                        .getTransactions(account.getApiIdentifier(), fromDate, toDate)
                        .getTinkTransactions(),
                false);
    }

    private boolean isInitialFetch() {
        return persistentStorage
                .get(StorageKeys.IS_INITIAL_FETCH, Boolean.class)
                .orElse(Boolean.FALSE);
    }

    private LocalDate getOldestDateForTransactionFetch() {
        return LocalDate.now(clock).minusMonths(MAX_NUM_MONTHS_FOR_FETCH);
    }

    private LocalDate getLocalDateFromDate(Date date) {
        return Objects.nonNull(date)
                ? date.toInstant().atZone(clock.getZone()).toLocalDate()
                : null;
    }
}
