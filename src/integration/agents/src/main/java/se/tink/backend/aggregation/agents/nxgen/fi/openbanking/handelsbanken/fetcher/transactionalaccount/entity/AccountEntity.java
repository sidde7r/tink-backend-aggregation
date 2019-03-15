package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.ExceptionMessages.ACCOUNT_TYPE_NOT_SUPPORTED;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.HandelsbankenConstants.Account;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity.BaseAccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class AccountEntity extends BaseAccountEntity {

    @Override
    public TransactionalAccount toTinkAccount(BalanceEntity balance) {
        return Account.TYPES.get(AccountTypes.CHECKING).stream()
            .filter(item -> item.equalsIgnoreCase(accountType))
            .findFirst()
            .map(account -> createCheckingAccount(balance))
            .orElseThrow(
                () -> new IllegalStateException(ACCOUNT_TYPE_NOT_SUPPORTED + accountType));
    }
}
