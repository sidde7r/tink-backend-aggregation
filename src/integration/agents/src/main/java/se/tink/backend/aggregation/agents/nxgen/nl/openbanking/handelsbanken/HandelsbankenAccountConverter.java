package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public class HandelsbankenAccountConverter implements HandelsbankenBaseAccountConverter {

    private final TypeMapper<TransactionalAccountType> accountTypes =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(TransactionalAccountType.CHECKING, "Betaalrekening")
                    .build();

    @Override
    public Optional<TransactionalAccount> toTinkAccount(
            AccountsItemEntity accountEntity, AccountDetailsResponse accountDetailsResponse) {
        return accountTypes
                .translate(accountEntity.getAccountType())
                .map(type -> accountEntity.toTinkAccount(type, accountDetailsResponse))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
