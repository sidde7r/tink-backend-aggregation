package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.nxgen.core.account.AccountHolderType;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

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

    default TransactionalAccountType mapTransactionalAccountType(AccountEntity account) {
        if ("CurrentAccount".equals(account.getRawAccountSubType())) {
            return TransactionalAccountType.CHECKING;
        } else if ("Savings".equals(account.getRawAccountSubType())) {
            return TransactionalAccountType.SAVINGS;
        }
        throw new IllegalStateException(
                "Cannot map to transactional account. Wrong account type passed to the mapper");
    }

    boolean supportsAccountType(AccountTypes type);

    Optional<T> map(
            AccountEntity account,
            Collection<AccountBalanceEntity> balances,
            Collection<PartyV31Entity> parties);
}
