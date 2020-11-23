package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.IdentityDataV31Entity;

public interface AccountMapper<T> {

    boolean supportsAccountType(AccountTypes type);

    Optional<T> map(
            AccountEntity account,
            Collection<AccountBalanceEntity> balances,
            Collection<IdentityDataV31Entity> parties);
}
