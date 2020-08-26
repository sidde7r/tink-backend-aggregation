package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.creditcard;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.creditcard.entity.CardAccountsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class HandelsbankenBaseCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionDatePaginator<CreditCardAccount> {

    private final HandelsbankenBaseApiClient apiClient;
    private final Date maxDate;

    public HandelsbankenBaseCreditCardFetcher(
            HandelsbankenBaseApiClient apiClient, LocalDate maxPeriodTransactions) {
        this.apiClient = apiClient;
        this.maxDate =
                Date.from(
                        maxPeriodTransactions
                                .atStartOfDay()
                                .atZone(ZoneId.systemDefault())
                                .toInstant());
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
        if (fromDate.compareTo(maxDate) < 0) {
            fromDate = maxDate;
        }

        if (fromDate.compareTo(toDate) >= 0) {
            return PaginatorResponseImpl.createEmpty();
        }

        return apiClient.getCreditTransactions(account.getApiIdentifier(), fromDate, toDate);
    }
}
