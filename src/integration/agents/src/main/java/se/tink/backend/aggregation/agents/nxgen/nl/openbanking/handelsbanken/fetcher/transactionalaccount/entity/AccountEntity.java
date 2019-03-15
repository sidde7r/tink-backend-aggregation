package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.ExceptionMessages.ACCOUNT_TYPE_NOT_SUPPORTED;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken.HandelsbankenConstants.Account;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity.BaseAccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class AccountEntity extends BaseAccountEntity {

    @Override
    public TransactionalAccount toTinkAccount(BalanceEntity balance) {
        if (Account.TYPES.get(AccountTypes.CHECKING).equalsIgnoreCase(accountType)) {
            return createCheckingAccount(balance);
        }
        throw new IllegalStateException(ACCOUNT_TYPE_NOT_SUPPORTED + accountType);
    }
}
