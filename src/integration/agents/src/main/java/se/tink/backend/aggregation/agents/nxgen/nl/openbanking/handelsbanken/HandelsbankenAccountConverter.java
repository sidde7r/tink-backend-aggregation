package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.ExceptionMessages.ACCOUNT_TYPE_NOT_SUPPORTED;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BaseAccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class HandelsbankenAccountConverter implements HandelsbankenBaseAccountConverter {
    @Override
    public TypeMapper<AccountTypes> getAccountTypeMapper() {
        return TypeMapper.<AccountTypes>builder()
                .put(AccountTypes.CHECKING, "Betaalrekening")
                .build();
    }

    @Override
    public TransactionalAccount toTinkAccount(
            BaseAccountEntity accountEntity, BalanceEntity balance) {
        return getAccountTypeMapper()
                .translate(accountEntity.getAccountType())
                .filter(AccountTypes.CHECKING::equals)
                .map(account -> accountEntity.createCheckingAccount(balance))
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        ACCOUNT_TYPE_NOT_SUPPORTED
                                                + accountEntity.getAccountType()));
    }
}
