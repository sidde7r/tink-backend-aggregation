package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BaseAccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public interface HandelsbankenBaseAccountConverter {
    Optional<TransactionalAccount> toTinkAccount(
            BaseAccountEntity accountEntity, BalanceEntity balance);
}
