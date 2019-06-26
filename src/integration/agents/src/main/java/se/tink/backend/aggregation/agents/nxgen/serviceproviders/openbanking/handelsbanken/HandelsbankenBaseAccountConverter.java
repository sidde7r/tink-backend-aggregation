package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItem;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalancesItem;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public interface HandelsbankenBaseAccountConverter {
    Optional<TransactionalAccount> toTinkAccount(AccountsItem accountEntity, BalancesItem balance);
}
