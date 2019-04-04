package se.tink.backend.aggregation.agents.nxgen.gb.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.ExceptionMessages.ACCOUNT_TYPE_NOT_SUPPORTED;

import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.gb.openbanking.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity.BaseAccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class AccountEntity extends BaseAccountEntity {

    @Override
    public TransactionalAccount toTinkAccount(BalanceEntity balance) {
        return HandelsbankenConstants.ACCOUNT_TYPE_MAPPER
                .translate(getAccountType())
                .map(account -> toTransactionalAccount(balance))
                .get();
    }

    private TransactionalAccount toTransactionalAccount(BalanceEntity balance) {
        Optional<AccountTypes> accountType =
                HandelsbankenConstants.ACCOUNT_TYPE_MAPPER.translate(getAccountType());

        if (accountType.filter(AccountTypes.CHECKING::equals).isPresent()) {
            return createCheckingAccount(balance);
        } else if (accountType.filter(AccountTypes.SAVINGS::equals).isPresent()) {
            return createSavingsAccount(balance);
        } else {
            throw new IllegalStateException(ACCOUNT_TYPE_NOT_SUPPORTED + accountType);
        }
    }
}
