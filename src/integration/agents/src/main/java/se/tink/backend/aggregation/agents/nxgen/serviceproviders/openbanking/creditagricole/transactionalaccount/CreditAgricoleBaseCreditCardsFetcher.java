package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class CreditAgricoleBaseCreditCardsFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionDatePaginator<CreditCardAccount> {

    private static final long MAX_NUM_MONTHS_FOR_FETCH = 13L;
    private static final long MAX_NUM_DAYS_FOR_FETCH_WITHOUT_SCA = 89L;

    private final CreditAgricoleBaseApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final LocalDateTimeSource localDateTimeSource;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        GetAccountsResponse getAccountsResponse = apiClient.getAccounts();

        List<AccountIdEntity> accountIdsForConsentRequest =
                getAccountsResponse.getAccountsListForNecessaryConsents();
        if (!accountIdsForConsentRequest.isEmpty()) {
            apiClient.putConsents(accountIdsForConsentRequest);
            getAccountsResponse = apiClient.getAccounts();
        }

        return getAccountsResponse.toTinkCreditCards();
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        final LocalDate fromDateLocal = getLocalDateFromDate(fromDate);
        final LocalDate toDateLocal = getLocalDateFromDate(toDate);

        return isInitialFetch()
                ? getAllTransactions(account, fromDateLocal, toDateLocal)
                : getMaxTransactionsWithoutSca(account, toDateLocal);
    }

    private PaginatorResponse getAllTransactions(
            CreditCardAccount account, LocalDate fromDate, LocalDate toDate) {

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

    private PaginatorResponse getMaxTransactionsWithoutSca(
            CreditCardAccount account, LocalDate toDate) {
        final LocalDate fromDate =
                localDateTimeSource
                        .now()
                        .toLocalDate()
                        .minusDays(MAX_NUM_DAYS_FOR_FETCH_WITHOUT_SCA);
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
        return localDateTimeSource.now().toLocalDate().minusMonths(MAX_NUM_MONTHS_FOR_FETCH);
    }

    private LocalDate getLocalDateFromDate(Date date) {
        return date != null ? date.toInstant().atZone(ZoneId.of("CET")).toLocalDate() : null;
    }
}
