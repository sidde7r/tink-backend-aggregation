package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class HandelsbankenBaseTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final HandelsbankenBaseApiClient apiClient;
    private final HandelsbankenBaseAccountConverter converter;
    private final Date maxDate;

    public HandelsbankenBaseTransactionalAccountFetcher(
            HandelsbankenBaseApiClient apiClient,
            HandelsbankenBaseAccountConverter converter,
            LocalDate maxPeriodTransactions) {
        this.apiClient = apiClient;
        this.converter = converter;
        this.maxDate =
                Date.from(
                        maxPeriodTransactions
                                .atStartOfDay()
                                .atZone(ZoneId.systemDefault())
                                .toInstant());
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.getAccountList().getAccounts().stream()
                .map(this::mapToTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> mapToTransactionalAccount(
            AccountsItemEntity accountEntity) {
        AccountDetailsResponse accountDetails =
                apiClient.getAccountDetails(accountEntity.getAccountId());
        return converter.toTinkAccount(accountEntity, accountDetails);
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        if (fromDate.compareTo(maxDate) < 0) {
            fromDate = maxDate;
        }

        if (fromDate.compareTo(toDate) >= 0) {
            return PaginatorResponseImpl.createEmpty();
        }

        return apiClient.getTransactions(account.getApiIdentifier(), fromDate, toDate);
    }
}
