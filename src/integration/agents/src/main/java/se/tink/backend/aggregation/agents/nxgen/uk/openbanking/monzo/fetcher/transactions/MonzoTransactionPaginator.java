package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactions;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.Time.DEFAULT_OFFSET;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.TransactionConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.UkOpenBankingTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class MonzoTransactionPaginator<T, S extends Account>
        extends UkOpenBankingTransactionPaginator<T, S> {

    private final TransactionPaginationHelper paginationHelper;
    private final UkOpenBankingAisConfig ukOpenBankingAisConfig;

    public MonzoTransactionPaginator(
            AgentComponentProvider componentProvider,
            Provider provider,
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            PersistentStorage persistentStorage,
            UkOpenBankingApiClient apiClient,
            Class<T> responseType,
            TransactionConverter<T, S> transactionConverter,
            LocalDateTimeSource localDateTimeSource,
            TransactionPaginationHelper paginationHelper) {
        super(
                componentProvider,
                provider,
                ukOpenBankingAisConfig,
                persistentStorage,
                apiClient,
                responseType,
                transactionConverter,
                localDateTimeSource);
        this.paginationHelper = paginationHelper;
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
        LocalDateTime fromDate =
                calculateFromBookingDate(account.getApiIdentifier()).toLocalDateTime();

        LocalDateTime historyTransactionDate =
                paginationHelper
                        .getTransactionDateLimit(account)
                        .map(date -> date.toInstant().atOffset(DEFAULT_OFFSET).toLocalDateTime())
                        .orElse(fromDate);

        if (historyTransactionDate.isAfter(fromDate)) {
            log.info(
                    "[MonzoTransactionPaginator] History transaction date is after proposed fromDate -> set historyTransactionDate as fromBookingDateTime to avoid fetching transactions which we already fetched in the past: fromDate is {} and historyTransactionDate is {}",
                    fromDate,
                    historyTransactionDate);
            return createKeyRequest(account, historyTransactionDate);
        }

        log.info(
                "[MonzoTransactionPaginator] No need for adjustments or first login by user -> fromDate: {}, lastTransactionDate: {}",
                fromDate,
                historyTransactionDate);
        return createKeyRequest(account, fromDate);
    }

    private String createKeyRequest(S account, LocalDateTime fromDate) {
        return ukOpenBankingAisConfig.getInitialTransactionsPaginationKey(
                        account.getApiIdentifier())
                + FROM_BOOKING_DATE_TIME
                + ISO_OFFSET_DATE_TIME.format(fromDate);
    }
}
