package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalancesItemEntity;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public class HandelsbankenAccountConverter implements HandelsbankenBaseAccountConverter {

    private final TypeMapper<TransactionalAccountType> accountTypes =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(TransactionalAccountType.CHECKING, "Current Account")
                    .put(TransactionalAccountType.SAVINGS, "Instant Access Deposit Account")
                    .build();

    @Override
    public Optional<TransactionalAccount> toTinkAccount(
            AccountsItemEntity accountEntity, BalancesItemEntity balance) {
        return accountTypes
                .translate(accountEntity.getAccountType())
                .map(type -> accountEntity.toTinkAccount(type, balance))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
