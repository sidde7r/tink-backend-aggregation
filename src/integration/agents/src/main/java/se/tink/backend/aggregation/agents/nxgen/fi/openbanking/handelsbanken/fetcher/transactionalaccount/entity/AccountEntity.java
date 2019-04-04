package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.ExceptionMessages.ACCOUNT_TYPE_NOT_SUPPORTED;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity.BaseAccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class AccountEntity extends BaseAccountEntity {

    private AccountTypes getTinkAccountType() {
        return HandelsbankenConstants.ACCOUNT_TYPE_MAPPER
                .translate(getAccountType())
                .orElse(AccountTypes.OTHER);
    }

    @Override
    public TransactionalAccount toTinkAccount(BalanceEntity balance) {
        if (getTinkAccountType().equals(AccountTypes.CHECKING)) {
            return createCheckingAccount(balance);
        }
        throw new IllegalStateException(ACCOUNT_TYPE_NOT_SUPPORTED + accountType);
    }
}
