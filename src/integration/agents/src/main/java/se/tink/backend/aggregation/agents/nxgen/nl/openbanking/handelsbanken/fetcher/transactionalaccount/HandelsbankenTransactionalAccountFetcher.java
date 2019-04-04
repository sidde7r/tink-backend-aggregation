package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.HandelsbankenBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class HandelsbankenTransactionalAccountFetcher
        extends HandelsbankenBaseTransactionalAccountFetcher<AccountsResponse, AccountEntity>
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    public HandelsbankenTransactionalAccountFetcher(HandelsbankenApiClient apiClient) {
        super(apiClient);
    }

    @Override
    protected boolean isAccountTypeSupported(AccountEntity account) {
        return HandelsbankenConstants.ACCOUNT_TYPE_MAPPER
                .translate(account.getAccountType())
                .isPresent();
    }

    @Override
    public Class<AccountsResponse> getResponseType() {
        return AccountsResponse.class;
    }
}
