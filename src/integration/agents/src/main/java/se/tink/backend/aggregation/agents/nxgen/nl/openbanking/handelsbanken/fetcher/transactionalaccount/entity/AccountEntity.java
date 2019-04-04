package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.ExceptionMessages.ACCOUNT_TYPE_NOT_SUPPORTED;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity.BaseAccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class AccountEntity extends BaseAccountEntity {

    @Override
    public TransactionalAccount toTinkAccount(BalanceEntity balance) {
        return HandelsbankenConstants.ACCOUNT_TYPE_MAPPER
                .translate(getAccountType())
                .filter(AccountTypes.CHECKING::equals)
                .map(account -> createCheckingAccount(balance))
                .orElseThrow(
                        () -> new IllegalStateException(ACCOUNT_TYPE_NOT_SUPPORTED + accountType));
    }
}
