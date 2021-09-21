package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactions;

import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.MonzoConstants.EUROPE_LONDON;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
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
        ZoneId zoneLondon = ZoneId.of(EUROPE_LONDON);
        Optional<Date> certainDate = getCertainDateFromCache(account);
        OffsetDateTime lastTransactionsFetchedDate =
                getLastTransactionsFetchedDate(account.getApiIdentifier());
        certainDate.ifPresent(
                value ->
                        log.info(
                                "[TRANSACTION PAGINATOR] Certain date is {}",
                                value.toInstant().atZone(zoneLondon).toLocalDateTime()));

        OffsetDateTime fromDate =
                certainDate
                        .map(
                                date ->
                                        date.toInstant()
                                                .atZone(zoneLondon)
                                                .minusDays(1)
                                                .toOffsetDateTime())
                        .filter(date -> date.isAfter(lastTransactionsFetchedDate))
                        .orElse(lastTransactionsFetchedDate);

        log.info(
                "[TRANSACTION PAGINATOR] Start fetching transactions from date {}",
                fromDate.toLocalDateTime());

        return createKeyRequest(account, lastTransactionsFetchedDate);
    }

    private String createKeyRequest(S account, OffsetDateTime fromDate) {
        return ukOpenBankingAisConfig.getInitialTransactionsPaginationKey(
                        account.getApiIdentifier())
                + FROM_BOOKING_DATE_TIME
                + ISO_OFFSET_DATE_TIME.format(fromDate);
    }

    private Optional<Date> getCertainDateFromCache(S account) {
        if (request.getAccounts().isEmpty()) {
            return Optional.empty();
        }
        return request.getAccounts().stream()
                .filter(a -> account.isUniqueIdentifierEqual(a.getBankId()))
                .map(se.tink.backend.agents.rpc.Account::getCertainDate)
                .findFirst();
    }
}
