package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.monzo.fetcher.transactions;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.Time.DEFAULT_ZONE_ID;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import se.tink.agent.sdk.operation.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.TransactionConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.UkOpenBankingTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class MonzoTransactionPaginator<T, S extends Account>
        extends UkOpenBankingTransactionPaginator<T, S> {

    private final TransactionPaginationHelper paginationHelper;

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
                localDateTimeSource,
                paginationHelper);
        this.paginationHelper = paginationHelper;
    }

    @Override
    protected String initialisePaginationKeyIfNull(S account, String key) {
        if (key != null) {
            return key;
        }
        LocalDateTime fromDate = calculateFromBookingDate(account.getApiIdentifier());

        LocalDateTime historyTransactionDate =
                paginationHelper
                        .getTransactionDateLimit(account)
                        .map(date -> date.toInstant().atZone(DEFAULT_ZONE_ID).toLocalDateTime())
                        .orElse(fromDate);

        if (historyTransactionDate.isAfter(fromDate)) {
            log.info(
                    "[MonzoTransactionPaginator] History transaction date is after proposed fromDate -> set historyTransactionDate as fromBookingDateTime to avoid fetching transactions which we already fetched in the past: fromDate is {} and historyTransactionDate is {}",
                    fromDate,
                    historyTransactionDate);
            return createRequestPaginationKey(account, historyTransactionDate);
        }

        log.info(
                "[MonzoTransactionPaginator] No need for adjustments or first login by user -> fromDate: {}, lastTransactionDate: {}",
                fromDate,
                historyTransactionDate);
        return createRequestPaginationKey(account, fromDate);
    }
}
