package se.tink.backend.aggregation.agents.nxgen.gb.openbanking.handelsbanken;

import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalancesItemEntity;
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
            AccountsItemEntity accountEntity, BalancesItemEntity balance) {
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
