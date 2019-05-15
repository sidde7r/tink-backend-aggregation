package se.tink.backend.aggregation.agents.nxgen.gb.openbanking.handelsbanken;

import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BaseAccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class HandelsbankenAccountConverter implements HandelsbankenBaseAccountConverter {
    @Override
    public TypeMapper<AccountTypes> getAccountTypeMapper() {
        return TypeMapper.<AccountTypes>builder()
                .put(AccountTypes.CHECKING, "Current Account")
                .put(AccountTypes.SAVINGS, "Instant Access Deposit Account")
                .build();
    }

    @Override
    public TransactionalAccount toTinkAccount(
            BaseAccountEntity accountEntity, BalanceEntity balance) {
        return getAccountTypeMapper()
                .translate(accountEntity.getAccountType())
                .map(account -> toTransactionalAccount(accountEntity, balance))
                .get();
    }

    private TransactionalAccount toTransactionalAccount(
            BaseAccountEntity accountEntity, BalanceEntity balance) {
        Optional<AccountTypes> accountType =
                getAccountTypeMapper().translate(accountEntity.getAccountType());

        if (accountType.filter(AccountTypes.CHECKING::equals).isPresent()) {
            return accountEntity.createCheckingAccount(balance);
        } else if (accountType.filter(AccountTypes.SAVINGS::equals).isPresent()) {
            return accountEntity.createSavingsAccount(balance);
        } else {
            throw new IllegalStateException(
                    HandelsbankenBaseConstants.ExceptionMessages.ACCOUNT_TYPE_NOT_SUPPORTED
                            + accountType);
        }
    }
}
