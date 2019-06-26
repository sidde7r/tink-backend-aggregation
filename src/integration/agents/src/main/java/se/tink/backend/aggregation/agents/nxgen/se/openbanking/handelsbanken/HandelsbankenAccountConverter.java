package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken;

import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItem;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalancesItem;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class HandelsbankenAccountConverter implements HandelsbankenBaseAccountConverter {

    private final TypeMapper<AccountTypes> accountTypes =
            TypeMapper.<AccountTypes>builder()
                    .put(
                            AccountTypes.CHECKING,
                            "Allkonto Ung",
                            "Allkonto",
                            "Checkkonto",
                            "Privatkonto")
                    .build();

    @Override
    public Optional<TransactionalAccount> toTinkAccount(
            AccountsItem accountEntity, BalancesItem balance) {
        return accountTypes
                .translate(accountEntity.getAccountType())
                .filter(AccountTypes.CHECKING::equals)
                .map(account -> accountEntity.createCheckingAccount(balance));
    }
}
