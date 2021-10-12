package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactions;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.TransactionConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.UkOpenBankingTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.HasRefreshScope;
import se.tink.libraries.credentials.service.RefreshScope;

@Slf4j
public class MonzoTransactionPaginator<T, S extends Account>
        extends UkOpenBankingTransactionPaginator<T, S> {

    private final CredentialsRequest request;
    private final UkOpenBankingAisConfig ukOpenBankingAisConfig;

    public MonzoTransactionPaginator(
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            PersistentStorage persistentStorage,
            UkOpenBankingApiClient apiClient,
            Class<T> responseType,
            TransactionConverter<T, S> transactionConverter,
            LocalDateTimeSource localDateTimeSource,
            CredentialsRequest request) {
        super(
                ukOpenBankingAisConfig,
                persistentStorage,
                apiClient,
                responseType,
                transactionConverter,
                localDateTimeSource);
        this.request = request;
        this.ukOpenBankingAisConfig = ukOpenBankingAisConfig;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(S account, String key) {
        updateAccountPaginationCount(account.getApiIdentifier());
        if (isPaginationCountOverLimit()) {
            return TransactionKeyPaginatorResponseImpl.createEmpty();
        }
        key = initialisePaginationKey(account, key);

        try {
            return fetchTransactions(account, key);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 401 || e.getResponse().getStatus() == 403) {
                return recover401Or403ResponseErrorStatus(account, e);
            }
            throw e;
        }
    }

    private String initialisePaginationKey(S account, String key) {
        if (key != null) {
            return key;
        }
        // 23m or 89d ago
        LocalDateTime fromDate =
                calculateFromBookingDate(account.getApiIdentifier()).toLocalDateTime();

        RefreshScope refreshScope = initialiseRefreshScopeIfEnabled();

        if (isTransactionHistoryProductEnabled(refreshScope)) {
            LocalDateTime historyTransactionsBooked =
                    refreshScope.getTransactions().getTransactionBookedDateGte().atStartOfDay();
            if (historyTransactionsBooked.isAfter(fromDate)) {
                log.info(
                        "[MonzoTransactionPaginator] Refresh scope transaction history date is after proposed fromDate -> set refreshScopeTransactionHistoryDate as fromBookingDateTime to avoid fetching transaction which we already fetched in the past: fromDate is {} and refreshScopeTransactionHistoryDate is {}",
                        fromDate,
                        historyTransactionsBooked);
                return createKeyRequest(account, fromDate);
            }
        } else {
            // A date before which we are (fairly) certain that no changes to transactions
            // will be made on the bank's side
            Optional<LocalDateTime> certainDate = getCertainDate(account);

            if (!certainDate.isPresent()) {
                log.info(
                        "[MonzoTransactionPaginator] No certainDate so this is first refresh ever made for this account -> fromDate is 23m ago: fromDate is {} and certainDate is null",
                        fromDate);
                return createKeyRequest(account, fromDate);
            }

            if (certainDate.get().isAfter(fromDate)) {
                log.info(
                        "[MonzoTransactionPaginator] Certain date is after proposed fromDate -> set certainDate as fromBookingDateTime to avoid fetching transaction which we already fetched in the past: fromDate is {} and certainDate is {}",
                        fromDate,
                        certainDate);
                return createKeyRequest(account, certainDate.get());
            }
        }

        log.info("[MonzoTransactionPaginator] No need for adjustments or first login by user");
        return createKeyRequest(account, fromDate);
    }

    private String createKeyRequest(S account, LocalDateTime fromDate) {
        return ukOpenBankingAisConfig.getInitialTransactionsPaginationKey(
                        account.getApiIdentifier())
                + FROM_BOOKING_DATE_TIME
                + ISO_OFFSET_DATE_TIME.format(fromDate);
    }

    private Optional<LocalDateTime> getCertainDate(S account) {
        if (request.getAccounts().isEmpty()) {
            return Optional.empty();
        }
        return request.getAccounts().stream()
                .filter(a -> account.isUniqueIdentifierEqual(a.getBankId()))
                .map(se.tink.backend.agents.rpc.Account::getCertainDate)
                .filter(Objects::nonNull)
                .map(d -> new java.sql.Timestamp(d.getTime()).toLocalDateTime())
                .findFirst();
    }

    private RefreshScope initialiseRefreshScopeIfEnabled() {
        if (request instanceof HasRefreshScope) {
            return ((HasRefreshScope) request).getRefreshScope();
        }
        log.debug(
                "Request of type {} does not implement {}, pagination helper will always return that it needs another page",
                request.getClass(),
                HasRefreshScope.class);

        return null;
    }

    private boolean isTransactionHistoryProductEnabled(RefreshScope refreshScope) {
        return refreshScope != null
                && refreshScope.getTransactions() != null
                && refreshScope.getTransactions().getTransactionBookedDateGte() != null;
    }
}
