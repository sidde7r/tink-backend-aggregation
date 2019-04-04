package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.AccountBalance;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.ExceptionMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity.BalancesEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity.BaseAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.rpc.BaseAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public abstract class HandelsbankenBaseTransactionalAccountFetcher<
                AccountsResponseType extends BaseAccountsResponse<AccountEntityType>,
                AccountEntityType extends BaseAccountEntity>
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    protected final HandelsbankenBaseApiClient apiClient;

    protected HandelsbankenBaseTransactionalAccountFetcher(HandelsbankenBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    protected AccountsResponseType fetchAccountsForType(Class<AccountsResponseType> type) {
        return apiClient.getAccountList(type);
    }

    protected TransactionalAccount mapToTransactionalAccount(BaseAccountEntity accountEntity) {
        BalancesEntity balances = apiClient.getAccountDetails(accountEntity.getAccountId());
        BalanceEntity availableBalance =
                balances.getBalances().stream()
                        .filter(
                                balance ->
                                        balance.getBalanceType()
                                                .equalsIgnoreCase(AccountBalance.TYPE))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                ExceptionMessages.BALANCE_NOT_FOUND));
        return accountEntity.toTinkAccount(availableBalance);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return fetchAccountsForType(getResponseType()).getAccounts().stream()
                .filter(this::isAccountTypeSupported)
                .map(this::mapToTransactionalAccount)
                .collect(Collectors.toList());
    }

    protected abstract boolean isAccountTypeSupported(AccountEntityType accountEntityType);

    public abstract Class<AccountsResponseType> getResponseType();

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return apiClient.getTransactions(
                account.getFromTemporaryStorage(StorageKeys.ACCOUNT_ID), fromDate, toDate);
    }
}
