package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

/**
 * Generic transaction paginator for ukob.
 *
 * @param <ResponseType> The transaction response entity
 * @param <AccountType> The type of account to fetch transactions for. eg. TransactionalAccount,
 *     CreditCard, etc.
 */
public class UkOpenBankingTransactionPaginator<ResponseType, AccountType extends Account>
        implements TransactionKeyPaginator<AccountType, String> {
    private static final int PAGINATION_LIMIT =
            50; // Limits number of pages fetched in order to reduce loading.
    private static final int PAGINATION_GRACE_LIMIT = 5;
    private static final long DEFAULT_MAX__ALLOWED_NUMBER_OF_MONTHS = 23l;
    // we can decrease this to 15 days also in future.
    private static final long DEFAULT_MAX_ALLOWED_DAYS = 89l;
    private static final String FROM_BOOKING_DATE_TIME = "?fromBookingDateTime=";
    private static final String FETCHED_TRANSACTIONS_UNTIL = "fetchedTxUntil:";
    private final UkOpenBankingApiClient apiClient;
    private final Class<ResponseType> responseType;
    private final TransactionConverter<ResponseType, AccountType> transactionConverter;
    private AggregationLogger logger =
            new AggregationLogger(UkOpenBankingTransactionPaginator.class);
    private String lastAccount;
    private int paginationCount;
    private final UkOpenBankingAisConfig ukOpenBankingAisConfig;
    private final PersistentStorage persistentStorage;

    /**
     * @param apiClient Ukob api client
     * @param responseType Class type of the account response entity
     * @param transactionConverter A method taking the TransactionEntity and a Tink account and
     *     converting it to a key pagination response. See: {@link
     *     se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.TransactionConverter#toPaginatorResponse(Object,
     *     Account)}
     */
    public UkOpenBankingTransactionPaginator(
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            PersistentStorage persistentStorage,
            UkOpenBankingApiClient apiClient,
            Class<ResponseType> responseType,
            TransactionConverter<ResponseType, AccountType> transactionConverter) {
        this.apiClient = apiClient;
        this.responseType = responseType;
        this.transactionConverter = transactionConverter;
        this.ukOpenBankingAisConfig = ukOpenBankingAisConfig;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            AccountType account, String key) {

        updateAccountPaginationCount(account.getBankIdentifier());

        if (paginationCount > PAGINATION_LIMIT) {
            return TransactionKeyPaginatorResponseImpl.createEmpty();
        }

        if (key == null) {
            final OffsetDateTime fromDate =
                    getLastTransactionsFetchedDate(account.getBankIdentifier());

            /*
            We need to send in fromDate when fetching transactions to improve the performance
            and also to adhere to OpenBanking standards of not fetching transactions more than 90
            days old with refresh token. For the very first time when we add the Bank to the app or
            fetch transactions we will try to fetch it for 23 months and after that for every refresh
            it will be for last 89 days.
            This is according to Article 10 of UkOpenBanking
            https://openbanking.atlassian.net/wiki/spaces/DZ/pages/1009778990/How+the+OBIE+Standard+can+be+used+in+relation+to+RTS+Article+10
             */

            key =
                    ukOpenBankingAisConfig.getInitialTransactionsPaginationKey(
                                    account.getBankIdentifier())
                            + FROM_BOOKING_DATE_TIME
                            + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(fromDate);
        }

        try {
            TransactionKeyPaginatorResponse<String> response =
                    transactionConverter.toPaginatorResponse(
                            apiClient.fetchAccountTransactions(
                                    ukOpenBankingAisConfig, key, responseType),
                            account);
            setFetchingTransactionsUntil(account.getBankIdentifier());
            return response;
        } catch (HttpResponseException e) {

            // NatWest seems to have an bug where they will send us next links even though it goes
            // out of range for how
            // many pages of transactions they actually can give us, causing an internal server
            // error.
            // This code ignores http 500 error if we have already fetched several pages from the
            // given account.
            if (paginationCount > PAGINATION_GRACE_LIMIT && e.getResponse().getStatus() == 500) {
                logger.warn("Ignoring http 500 (Internal server error) in pagination.", e);
                return TransactionKeyPaginatorResponseImpl.createEmpty();
            }

            /*
            There will be cases when credentials are already created and we try to use refresh tokens
            to fetch the transactions. Since the call to fetchTransactions will try to fetch
            transactions for last 23 months and this might result in 401 so if that is the case then
            we should try to fetch the transactions for last 89 days which should work.
            401 is to cover Danske as they send 401 instead of 403.
             */
            if (e.getResponse().getStatus() == 401 || e.getResponse().getStatus() == 403) {
                logger.debug(
                        "Trying to fetch transactions again for last 89 days. Got 401 in previous request",
                        e);

                key =
                        ukOpenBankingAisConfig.getInitialTransactionsPaginationKey(
                                        account.getBankIdentifier())
                                + FROM_BOOKING_DATE_TIME
                                + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                                        LocalDateTime.now().minusDays(DEFAULT_MAX_ALLOWED_DAYS));
                TransactionKeyPaginatorResponse<String> response =
                        transactionConverter.toPaginatorResponse(
                                apiClient.fetchAccountTransactions(
                                        ukOpenBankingAisConfig, key, responseType),
                                account);
                setFetchingTransactionsUntil(account.getBankIdentifier());
                return response;
            }

            throw e;
        }
    }

    private void updateAccountPaginationCount(String accountBankIdentifier) {

        if (!accountBankIdentifier.equalsIgnoreCase(lastAccount)) {
            paginationCount = 0;
        }

        lastAccount = accountBankIdentifier;
        paginationCount++;
    }

    private void setFetchingTransactionsUntil(String accountId) {
        final String fetchedUntilDate =
                OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        persistentStorage.put(FETCHED_TRANSACTIONS_UNTIL + accountId, fetchedUntilDate);
    }

    private Optional<OffsetDateTime> fetchedTransactionsUntil(String accountId) {
        final String dateString = persistentStorage.get(FETCHED_TRANSACTIONS_UNTIL + accountId);
        if (Objects.isNull(dateString)) {
            return Optional.empty();
        }
        try {
            return Optional.of(
                    OffsetDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        } catch (DateTimeParseException e) {
            /*
            This implies the format saved in persistent storage is ISO_LOCAL_DATE_TIME so this
            needs to be converted to ISO_OFFSET_DATE_TIME.
             */
            LocalDateTime timeInStorage =
                    LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return Optional.of(OffsetDateTime.of(timeInStorage, ZoneOffset.UTC));
        }
    }

    private OffsetDateTime getLastTransactionsFetchedDate(String accountId) {
        final Optional<OffsetDateTime> lastTransactionsFetchedDate =
                fetchedTransactionsUntil(accountId);
        final OffsetDateTime defaultRefreshDate =
                OffsetDateTime.now().minusDays(DEFAULT_MAX_ALLOWED_DAYS);
        if (lastTransactionsFetchedDate.isPresent()
                && lastTransactionsFetchedDate.get().isAfter(defaultRefreshDate)) {
            return defaultRefreshDate;
        } else {
            return OffsetDateTime.now().minusMonths(DEFAULT_MAX__ALLOWED_NUMBER_OF_MONTHS);
        }
    }
}
