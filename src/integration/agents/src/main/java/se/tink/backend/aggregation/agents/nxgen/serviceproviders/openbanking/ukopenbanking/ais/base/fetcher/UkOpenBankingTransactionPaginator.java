package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.Time.DEFAULT_ZONE_ID;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import se.tink.agent.sdk.operation.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.storage.data.FetchedTransactionsDataStorage;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;
import se.tink.libraries.unleash.model.UnleashContextWrapper;

/**
 * Generic transaction paginator for ukob.
 *
 * @param <ResponseType> The transaction response entity
 * @param <AccountType> The type of account to fetch transactions for. eg. TransactionalAccount,
 *     CreditCard, etc.
 */
@Slf4j
public class UkOpenBankingTransactionPaginator<ResponseType, AccountType extends Account>
        implements TransactionKeyPaginator<AccountType, String> {

    public static final DateTimeFormatter ISO_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    // we can decrease this to 15 days also in future.
    protected static final long DEFAULT_MAX_ALLOWED_DAYS = 89;
    protected static final String FROM_BOOKING_DATE_TIME = "?fromBookingDateTime=";
    private static final int PAGINATION_LIMIT =
            50; // Limits number of pages fetched in order to reduce loading.
    private static final long DEFAULT_MAX_ALLOWED_NUMBER_OF_MONTHS = 23;

    protected final LocalDateTimeSource localDateTimeSource;
    protected final UkOpenBankingApiClient apiClient;
    protected final Class<ResponseType> responseType;
    private final TransactionConverter<ResponseType, AccountType> transactionConverter;
    private final UkOpenBankingAisConfig ukOpenBankingAisConfig;
    private final FetchedTransactionsDataStorage fetchedTransactionsDataStorage;
    private final TransactionPaginationHelper paginationHelper;
    private final UnleashClient unleashClient;
    private final Toggle toggle;
    private String lastAccount;
    private int paginationCount;

    /**
     * @param apiClient Ukob api client
     * @param responseType Class type of the account response entity
     * @param transactionConverter A method taking the TransactionEntity and a Tink account and
     *     converting it to a key pagination response. See: {@link
     *     TransactionConverter#toPaginatorResponse(Object, Account)}
     */
    public UkOpenBankingTransactionPaginator(
            AgentComponentProvider componentProvider,
            Provider provider,
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            PersistentStorage persistentStorage,
            UkOpenBankingApiClient apiClient,
            Class<ResponseType> responseType,
            TransactionConverter<ResponseType, AccountType> transactionConverter,
            LocalDateTimeSource localDateTimeSource,
            TransactionPaginationHelper paginationHelper) {
        this.apiClient = apiClient;
        this.responseType = responseType;
        this.transactionConverter = transactionConverter;
        this.ukOpenBankingAisConfig = ukOpenBankingAisConfig;
        this.fetchedTransactionsDataStorage = new FetchedTransactionsDataStorage(persistentStorage);
        this.localDateTimeSource = localDateTimeSource;
        this.paginationHelper = paginationHelper;
        this.unleashClient = componentProvider.getUnleashClient();
        this.toggle =
                Toggle.of("UK_SET_MAX_ALLOWED_NUMBER_OF_MONTHS_TO_24")
                        .unleashContextWrapper(
                                UnleashContextWrapper.builder()
                                        .providerName(provider.getName())
                                        .appId(componentProvider.getContext().getAppId())
                                        .build())
                        .build();
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
            if (shouldRecoverFetchingTransactions(e.getResponse().getStatus())) {
                return recoverFetchingTransactions(account, key, e);
            }
            throw e;
        }
    }

    protected TransactionKeyPaginatorResponse<String> fetchTransactions(
            AccountType account, String key) {
        LocalDateTime requestTime = localDateTimeSource.now(DEFAULT_ZONE_ID);
        TransactionKeyPaginatorResponse<String> response =
                transactionConverter.toPaginatorResponse(
                        apiClient.fetchAccountTransactions(key, responseType), account);
        String accountID = account.getApiIdentifier();
        fetchedTransactionsDataStorage.setFetchedTransactionsUntil(accountID, requestTime);
        return response;
    }

    /**
     * The below method covers two cases when we got 401 or 403 in the response from banks:
     *
     * <p>1. When we try to use refresh tokens to fetch the transactions and our logic calculated
     * that we should fetch transactions from the last 23 months we will receive 401 or 403
     * responses from the bank because during refresh we should fetch only the last 90 days. So to
     * avoid finishing fetching transactions with some ERROR we want to create a new 'key' with
     * setting fromBookingDate to the last 90 days.
     *
     * <p>2. When during fetching transactions (after initial transactions request; I mean when
     * 'nextPage' is in use) we receive an error with status 401 or 403 then we want to retry the
     * last request to avoid duplicates transactions.
     */
    protected TransactionKeyPaginatorResponse<String> recoverFetchingTransactions(
            AccountType account, String key, HttpResponseException e) {
        if (isFirstPage()) {
            log.warn(
                    "[UKOpenBanking Transaction Paginator] The first request fetching transactions has been failed with key {}.",
                    key);
            LocalDateTime time =
                    localDateTimeSource.now(DEFAULT_ZONE_ID).minusDays(DEFAULT_MAX_ALLOWED_DAYS);
            key = createRequestPaginationKey(account, time);
        }
        log.warn(
                "[UKOpenBanking Transaction Paginator] Recover fetching transactions for key {}. Got {} in previous request with the below exception\n{}",
                key,
                e.getResponse().getStatus(),
                ExceptionUtils.getStackTrace(e));

        return fetchTransactions(account, key);
    }

    protected String initialisePaginationKeyIfNull(AccountType account, String key) {
        if (key == null) {
            final LocalDateTime fromDate = calculateFromBookingDate(account.getApiIdentifier());
            if (!ukOpenBankingAisConfig.isFetchingTransactionsFromTheNewestToTheOldest()) {
                return createRequestPaginationKey(
                        account, getTransactionDateLimit(account, fromDate));
            }

            return createRequestPaginationKey(account, fromDate);
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

    protected LocalDateTime calculateFromBookingDate(String accountId) {
        final Optional<LocalDateTime> dateOfLastTransactionFetching =
                fetchedTransactionsDataStorage.getFetchedTransactionsUntil(accountId);

        final LocalDateTime now = localDateTimeSource.now(DEFAULT_ZONE_ID);
        final LocalDateTime startingDateForFetchingRecentTransactions =
                now.minusDays(DEFAULT_MAX_ALLOWED_DAYS);

        final LocalDateTime startingDateForFetchingAsMuchAsPossible =
                calculateBiggestStartingDateForFetching(now);

        log.info(
                "dateOfLastTransactionFetching.isPresent(): "
                        + dateOfLastTransactionFetching.isPresent());
        if (dateOfLastTransactionFetching.isPresent()
                && dateOfLastTransactionFetching
                        .get()
                        .isAfter(startingDateForFetchingRecentTransactions)) {
            log.info(
                    "dateOfLastTransactionFetching ("
                            + dateOfLastTransactionFetching.get()
                            + ") isAfter startingDateForFetchingRecentTransactions ("
                            + startingDateForFetchingRecentTransactions
                            + ")");
            return startingDateForFetchingRecentTransactions;
        } else {
            return startingDateForFetchingAsMuchAsPossible;
        }
    }

    protected boolean shouldRecoverFetchingTransactions(int status) {
        return status == 401 || status == 403;
    }

    protected String createRequestPaginationKey(AccountType account, LocalDateTime fromDate) {
        return ukOpenBankingAisConfig.getInitialTransactionsPaginationKey(
                        account.getApiIdentifier())
                + FROM_BOOKING_DATE_TIME
                + ISO_DATE_TIME_FORMATTER.format(fromDate);
    }

    private boolean isFirstPage() {
        return paginationCount == 1;
    }

    private LocalDateTime calculateBiggestStartingDateForFetching(LocalDateTime now) {
        LocalDateTime startingDateForFetchingAsMuchAsPossible;
        boolean is24Months = unleashClient.isToggleEnabled(toggle);
        if (is24Months) {
            startingDateForFetchingAsMuchAsPossible = now.minusMonths(24);
        } else {
            startingDateForFetchingAsMuchAsPossible =
                    now.minusMonths(DEFAULT_MAX_ALLOWED_NUMBER_OF_MONTHS);
        }
        log.info(
                "[UK Transaction Paginator] Fetching transactions since "
                        + startingDateForFetchingAsMuchAsPossible);
        return startingDateForFetchingAsMuchAsPossible;
    }

    private LocalDateTime getTransactionDateLimit(AccountType account, LocalDateTime fromDate) {
        LocalDateTime historyTransactionDate =
                paginationHelper
                        .getTransactionDateLimit(account)
                        .map(date -> date.toInstant().atZone(DEFAULT_ZONE_ID).toLocalDateTime())
                        .orElse(fromDate);

        if (historyTransactionDate.isAfter(fromDate)) {
            log.info(
                    "[UkOpenBankingTransactionPaginator] History transaction date is after proposed fromDate -> set historyTransactionDate as fromBookingDateTime to avoid fetching transactions which we already fetched in the past: fromDate is {} and historyTransactionDate is {}",
                    fromDate,
                    historyTransactionDate);
            return historyTransactionDate;
        }
        log.info(
                "[UkOpenBankingTransactionPaginator] No need for adjustments or first login by user -> fromDate: {}, lastTransactionDate: {}",
                fromDate,
                historyTransactionDate);
        return fromDate;
    }
}
