package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
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
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int PAGINATION_LIMIT =
            50; // Limits number of pages fetched in order to reduce loading.
    private static final long DEFAULT_MAX_ALLOWED_NUMBER_OF_MONTHS = 23;
    // we can decrease this to 15 days also in future.
    protected static final long DEFAULT_MAX_ALLOWED_DAYS = 89;
    protected static final String FROM_BOOKING_DATE_TIME = "?fromBookingDateTime=";
    private static final String FETCHED_TRANSACTIONS_UNTIL = "fetchedTxUntil:";
    protected final UkOpenBankingApiClient apiClient;
    protected final Class<ResponseType> responseType;
    private final TransactionConverter<ResponseType, AccountType> transactionConverter;

    private String lastAccount;
    private int paginationCount;
    private final UkOpenBankingAisConfig ukOpenBankingAisConfig;
    private final PersistentStorage persistentStorage;
    protected final LocalDateTimeSource localDateTimeSource;

    /**
     * @param apiClient Ukob api client
     * @param responseType Class type of the account response entity
     * @param transactionConverter A method taking the TransactionEntity and a Tink account and
     *     converting it to a key pagination response. See: {@link
     *     TransactionConverter#toPaginatorResponse(Object, Account)}
     */
    public UkOpenBankingTransactionPaginator(
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            PersistentStorage persistentStorage,
            UkOpenBankingApiClient apiClient,
            Class<ResponseType> responseType,
            TransactionConverter<ResponseType, AccountType> transactionConverter,
            LocalDateTimeSource localDateTimeSource) {
        this.apiClient = apiClient;
        this.responseType = responseType;
        this.transactionConverter = transactionConverter;
        this.ukOpenBankingAisConfig = ukOpenBankingAisConfig;
        this.persistentStorage = persistentStorage;
        this.localDateTimeSource = localDateTimeSource;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            AccountType account, String key) {

        updateAccountPaginationCount(account.getApiIdentifier());
        if (isPaginationCountOverLimit()) {
            return TransactionKeyPaginatorResponseImpl.createEmpty();
        }
        key = initialisePaginationKeyIfNull(account, key);

        try {
            return fetchTransactions(account, key);
        } catch (HttpResponseException e) {

            if (e.getResponse().getStatus() == 401 || e.getResponse().getStatus() == 403) {
                return handle401Or403ResponseErrorStatus(account, e);
            }
            throw e;
        }
    }

    protected TransactionKeyPaginatorResponse<String> fetchTransactions(
            AccountType account, String key) {
        LocalDateTime requestTime = localDateTimeSource.now();
        TransactionKeyPaginatorResponse<String> response =
                transactionConverter.toPaginatorResponse(
                        apiClient.fetchAccountTransactions(key, responseType), account);
        setFetchingTransactionsUntil(account.getApiIdentifier(), requestTime);
        return response;
    }

    protected TransactionKeyPaginatorResponse<String> handle401Or403ResponseErrorStatus(
            AccountType account, HttpResponseException e) {
        /*
        There will be cases when credentials are already created and we try to use refresh tokens
        to fetch the transactions. Since the call to fetchTransactions will try to fetch
        transactions for last 23 months and this might result in 401 so if that is the case then
        we should try to fetch the transactions for last 89 days which should work.
        401 is to cover Danske as they send 401 instead of 403.
         */
        String key;
        logger.error(
                "Trying to fetch transactions again for last 89 days. Got 401 in previous request",
                e);

        key =
                ukOpenBankingAisConfig.getInitialTransactionsPaginationKey(
                                account.getApiIdentifier())
                        + FROM_BOOKING_DATE_TIME
                        + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                                localDateTimeSource.now().minusDays(DEFAULT_MAX_ALLOWED_DAYS));
        return fetchTransactions(account, key);
    }

    protected String initialisePaginationKeyIfNull(AccountType account, String key) {
        if (key == null) {
            final OffsetDateTime fromDate =
                    getLastTransactionsFetchedDate(account.getApiIdentifier());

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
                                    account.getApiIdentifier())
                            + FROM_BOOKING_DATE_TIME
                            + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(fromDate);
        }
        return key;
    }

    protected boolean isPaginationCountOverLimit() {
        return paginationCount > PAGINATION_LIMIT;
    }

    protected void updateAccountPaginationCount(String accountBankIdentifier) {

        if (!accountBankIdentifier.equalsIgnoreCase(lastAccount)) {
            paginationCount = 0;
        }

        lastAccount = accountBankIdentifier;
        paginationCount++;
    }

    private void setFetchingTransactionsUntil(String accountId, LocalDateTime requestTime) {
        final String fetchedUntilDate = requestTime.format(DateTimeFormatter.ISO_DATE_TIME);
        persistentStorage.put(FETCHED_TRANSACTIONS_UNTIL + accountId, fetchedUntilDate);
    }

    private Optional<OffsetDateTime> fetchedTransactionsUntil(String accountId) {
        String dateString = persistentStorage.get(FETCHED_TRANSACTIONS_UNTIL + accountId);
        return Optional.ofNullable(dateString)
                .map(
                        date ->
                                LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME)
                                        .atOffset(ZoneOffset.UTC));
    }

    protected OffsetDateTime getLastTransactionsFetchedDate(String accountId) {
        final Optional<OffsetDateTime> lastTransactionsFetchedDate =
                fetchedTransactionsUntil(accountId);

        LocalDateTime now = localDateTimeSource.now();

        final OffsetDateTime defaultRefreshDate =
                now.minusDays(DEFAULT_MAX_ALLOWED_DAYS).atOffset(ZoneOffset.UTC);
        if (lastTransactionsFetchedDate.isPresent()
                && lastTransactionsFetchedDate.get().isAfter(defaultRefreshDate)) {
            return defaultRefreshDate;
        } else {
            return now.minusMonths(DEFAULT_MAX_ALLOWED_NUMBER_OF_MONTHS).atOffset(ZoneOffset.UTC);
        }
    }
}
