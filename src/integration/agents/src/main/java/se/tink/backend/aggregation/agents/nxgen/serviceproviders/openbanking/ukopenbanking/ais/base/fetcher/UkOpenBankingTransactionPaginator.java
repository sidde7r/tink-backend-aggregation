package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.Time.DEFAULT_OFFSET;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.Time.DEFAULT_ZONE_ID;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import no.finn.unleash.UnleashContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;
import se.tink.libraries.unleash.strategies.aggregation.providersidsandexcludeappids.Constants;

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

    private static final int PAGINATION_LIMIT =
            50; // Limits number of pages fetched in order to reduce loading.
    private static final long DEFAULT_MAX_ALLOWED_NUMBER_OF_MONTHS = 23;
    // we can decrease this to 15 days also in future.
    protected static final long DEFAULT_MAX_ALLOWED_DAYS = 89;
    protected static final String FROM_BOOKING_DATE_TIME = "?fromBookingDateTime=";
    private static final String FETCHED_TRANSACTIONS_UNTIL = "fetchedTxUntil:";
    public static final DateTimeFormatter ISO_OFFSET_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    protected final UkOpenBankingApiClient apiClient;
    protected final Class<ResponseType> responseType;
    private final TransactionConverter<ResponseType, AccountType> transactionConverter;

    private String lastAccount;
    private int paginationCount;
    private UnleashClient unleashClient;
    private Toggle toggle;
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
            AgentComponentProvider componentProvider,
            Provider provider,
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
        unleashClient = componentProvider.getUnleashClient();
        toggle =
                Toggle.of("UK_SET_MAX_ALLOWED_NUMBER_OF_MONTHS_TO_24")
                        .context(
                                UnleashContext.builder()
                                        .addProperty(
                                                Constants.Context.PROVIDER_NAME.getValue(),
                                                provider.getName())
                                        .addProperty(
                                                Constants.Context.APP_ID.getValue(),
                                                componentProvider.getContext().getAppId())
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
            if (e.getResponse().getStatus() == 401 || e.getResponse().getStatus() == 403) {
                return recover401Or403ResponseErrorStatus(account, key, e);
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

    /**
     * Sometimes during fetching transactions, we can receive 401 or 403 error responses for the
     * second or third requests (I mean `nextPage` requests) and it is a cause of duplicated
     * transactions because collecting transactions we keep on the higher level and we are not able
     * to check if we have duplicated transactions so we decided to prepare retry with the last used
     * key. But if it will be the first request for example for 23 months and we will receive 401 or
     * 403 error code then we prepare a new request for the last 90 days of transactions.
     */
    protected TransactionKeyPaginatorResponse<String> recover401Or403ResponseErrorStatus(
            AccountType account, String key, HttpResponseException e) {
        if (isFirstPage()) {
            key =
                    ukOpenBankingAisConfig.getInitialTransactionsPaginationKey(
                                    account.getApiIdentifier())
                            + FROM_BOOKING_DATE_TIME
                            + ISO_OFFSET_DATE_TIME.format(
                                    localDateTimeSource
                                            .now(DEFAULT_ZONE_ID)
                                            .atOffset(DEFAULT_OFFSET)
                                            .minusDays(DEFAULT_MAX_ALLOWED_DAYS));
        }
        log.warn(
                "Retry fetching transactions for key {}. Got {} in previous request with the below exception\n{}",
                key,
                e.getResponse().getStatus(),
                ExceptionUtils.getStackTrace(e));

        return fetchTransactions(account, key);
    }

    protected boolean isFirstPage() {
        return paginationCount == 1;
    }

    protected String initialisePaginationKeyIfNull(AccountType account, String key) {
        if (key == null) {
            final OffsetDateTime fromDate = calculateFromBookingDate(account.getApiIdentifier());

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
                            + ISO_OFFSET_DATE_TIME.format(fromDate);
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
                                        .atOffset(DEFAULT_OFFSET));
    }

    protected OffsetDateTime calculateFromBookingDate(String accountId) {
        final Optional<OffsetDateTime> dateOfLastTransactionFetching =
                fetchedTransactionsUntil(accountId);

        final OffsetDateTime now = localDateTimeSource.now().atOffset(DEFAULT_OFFSET);
        final OffsetDateTime startingDateForFetchingRecentTransactions =
                now.minusDays(DEFAULT_MAX_ALLOWED_DAYS);

        final OffsetDateTime startingDateForFetchingAsMuchAsPossible =
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

    private OffsetDateTime calculateBiggestStartingDateForFetching(OffsetDateTime now) {
        OffsetDateTime startingDateForFetchingAsMuchAsPossible;
        boolean is24Months = unleashClient.isToggleEnable(toggle);
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
}
