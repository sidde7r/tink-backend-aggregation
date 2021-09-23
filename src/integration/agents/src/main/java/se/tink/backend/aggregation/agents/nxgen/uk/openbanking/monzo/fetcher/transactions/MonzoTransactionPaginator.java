package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactions;

import java.time.LocalDate;
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

        // 23m or 89d ago
        LocalDate fromDate = calculateFromBookingDate(account.getApiIdentifier()).toLocalDate();

        // A date before which we are (fairly) certain that no changes to transactions
        // will be made on the bank's side
        Optional<LocalDate> certainDate = getCertainDate(account);

        // Certain date is missing, so this is first refresh ever made for this account
        // -> fromDate is 23m ago
        if (!certainDate.isPresent()) {
            return createKeyRequest(account, fromDate);
        }

        // Certain date is newer than proposed fromDate -> Avoid fetching transaction
        // which we already have in database by using certain date as fromBookingDateTime
        if (certainDate.get().isAfter(fromDate)) {
            return createKeyRequest(account, certainDate.get());
        }

        // Certain date is older or equal to proposed fromDate -> No need for adjustments
        return createKeyRequest(account, fromDate);
    }

    private String createKeyRequest(S account, LocalDate fromDate) {
        return ukOpenBankingAisConfig.getInitialTransactionsPaginationKey(
                        account.getApiIdentifier())
                + FROM_BOOKING_DATE_TIME
                + ISO_OFFSET_DATE_TIME.format(fromDate);
    }

    private Optional<LocalDate> getCertainDate(S account) {
        if (request.getAccounts().isEmpty()) {
            return Optional.empty();
        }
        return request.getAccounts().stream()
                .filter(a -> account.isUniqueIdentifierEqual(a.getBankId()))
                .map(se.tink.backend.agents.rpc.Account::getCertainDate)
                .map(d -> new java.sql.Date(d.getTime()).toLocalDate())
                .findFirst();
    }
}
