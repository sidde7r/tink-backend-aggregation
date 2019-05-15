package se.tink.backend.aggregation.agents.nxgen.gb.openbanking.handelsbanken;

import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BaseAccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class HandelsbankenAccountConverter implements HandelsbankenBaseAccountConverter {

    private final TypeMapper<AccountTypes> accountTypes =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "Current Account")
                    .put(AccountTypes.SAVINGS, "Instant Access Deposit Account")
                    .build();

    @Override
    public Optional<TransactionalAccount> toTinkAccount(
            BaseAccountEntity accountEntity, BalanceEntity balance) {
        return accountTypes
                .translate(accountEntity.getAccountType())
                .map(
                        accountTypes -> {
                            if (accountTypes.equals(AccountTypes.CHECKING)) {
                                return accountEntity.createCheckingAccount(balance);
                            } else {
                                return accountEntity.createSavingsAccount(balance);
                            }
                        });
    }
}
