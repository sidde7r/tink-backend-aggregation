package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.nxgen.core.account.AccountHolderType;

public interface AccountMapper<T> {

    String PERSONAL_TYPE = "Personal";
    String BUSINESS_TYPE = "Business";

    default AccountHolderType mapAccountHolderType(AccountEntity account) {
        if (PERSONAL_TYPE.equalsIgnoreCase(account.getRawAccountType())) {
            return AccountHolderType.PERSONAL;
        } else if (BUSINESS_TYPE.equalsIgnoreCase(account.getRawAccountType())) {
            return AccountHolderType.BUSINESS;
        }

        return AccountHolderType.UNKNOWN;
    }

    boolean supportsAccountType(AccountTypes type);

    Optional<T> map(
            AccountEntity account,
            Collection<AccountBalanceEntity> balances,
            Collection<PartyV31Entity> parties);
}
