package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.creditcard;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.creditcard.entity.CardAccountsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class HandelsbankenBaseCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionDatePaginator<CreditCardAccount> {

    private final HandelsbankenBaseApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public HandelsbankenBaseCreditCardFetcher(
            HandelsbankenBaseApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.getCreditAccounts().getCardAccounts().stream()
                .map(CardAccountsEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        fromDate = checkMaxDate(fromDate);

        try {
            return apiClient.getCreditTransactions(account.getApiIdentifier(), fromDate, toDate);
        } catch (HttpResponseException e) {
            String exceptionMessage = e.getResponse().getBody(String.class);
            if (exceptionMessage.contains("Invalid time interval")) {
                return PaginatorResponseImpl.createEmpty(false);
            }
            throw e;
        }
    }

    private Date checkMaxDate(Date fromDate) {
        Optional<Date> maxDate = getMaxDateFromSession();
        if (maxDate.isPresent() && fromDate.compareTo(maxDate.get()) < 0) {
            return maxDate.get();
        }

        return fromDate;
    }

    private Optional<Date> getMaxDateFromSession() {
        return persistentStorage.get(
                HandelsbankenBaseConstants.StorageKeys.MAX_FETCH_PERIOD_MONTHS, Date.class);
    }
}
