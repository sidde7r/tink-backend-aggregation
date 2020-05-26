package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalancesItemEntity;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public class HandelsbankenAccountConverter implements HandelsbankenBaseAccountConverter {

    private static final TypeMapper<TransactionalAccountType> ACCOUNT_TYPES =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            "Käyttötili",
                            "Tuottotili",
                            "Monitili",
                            "Valuuttatili")
                    .build();

    @Override
    public Optional<TransactionalAccount> toTinkAccount(
            AccountsItemEntity accountEntity, BalancesItemEntity balance) {
        return ACCOUNT_TYPES
                .translate(accountEntity.getAccountType())
                .map(type -> accountEntity.toTinkAccount(type, balance))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
