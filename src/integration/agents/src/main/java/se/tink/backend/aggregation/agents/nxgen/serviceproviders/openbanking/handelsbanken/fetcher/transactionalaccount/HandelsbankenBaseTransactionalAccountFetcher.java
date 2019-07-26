package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.ExceptionMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalancesItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.BalanceAccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class HandelsbankenBaseTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final HandelsbankenBaseApiClient apiClient;
    private HandelsbankenBaseAccountConverter converter;
    private SessionStorage sessionStorage;

    public HandelsbankenBaseTransactionalAccountFetcher(
            HandelsbankenBaseApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public void setConverter(HandelsbankenBaseAccountConverter converter) {
        this.converter = converter;
    }

    private Optional<TransactionalAccount> mapToTransactionalAccount(
            AccountsItemEntity accountEntity) {
        BalanceAccountResponse balances = apiClient.getAccountDetails(accountEntity.getAccountId());
        BalancesItemEntity availableBalance =
                balances.getBalances().stream()
                        .filter(BalancesItemEntity::isBalance)
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                ExceptionMessages.BALANCE_NOT_FOUND));
        return converter.toTinkAccount(accountEntity, availableBalance);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.getAccountList().getAccounts().stream()
                .map(this::mapToTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        fromDate = checkMaxDate(fromDate);

        try {
            return apiClient.getTransactions(account.getApiIdentifier(), fromDate, toDate);
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
        if (maxDate.isPresent()) {
            if (fromDate.compareTo(maxDate.get()) < 0) {
                return maxDate.get();
            }
        }
        return fromDate;
    }

    private Optional<Date> getMaxDateFromSession() {
        return sessionStorage.get(
                HandelsbankenBaseConstants.StorageKeys.MAX_FETCH_PERIOD_MONTHS, Date.class);
    }
}
