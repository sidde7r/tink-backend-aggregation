package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.AccountBalance;
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

public class HandelsbankenBaseTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final HandelsbankenBaseApiClient apiClient;
    private HandelsbankenBaseAccountConverter converter;

    public HandelsbankenBaseTransactionalAccountFetcher(HandelsbankenBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void setConverter(HandelsbankenBaseAccountConverter converter) {
        this.converter = converter;
    }

    private Optional<TransactionalAccount> mapToTransactionalAccount(
            AccountsItemEntity accountEntity) {
        BalanceAccountResponse balances = apiClient.getAccountDetails(accountEntity.getAccountId());
        BalancesItemEntity availableBalance =
                balances.getBalances().stream()
                        .filter(
                                balance ->
                                        balance.getBalanceType()
                                                .equalsIgnoreCase(AccountBalance.AVAILABLE_BALANCE))
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
        try {
            return apiClient.getTransactions(account.getApiIdentifier(), fromDate, toDate);
        } catch (HttpResponseException h) {
            return PaginatorResponseImpl.createEmpty(false);
        }
    }
}
