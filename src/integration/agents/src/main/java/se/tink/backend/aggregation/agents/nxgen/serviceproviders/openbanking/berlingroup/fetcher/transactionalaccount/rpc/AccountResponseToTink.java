package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public interface AccountResponseToTink {
    Collection<TransactionalAccount> toTinkAccounts();

    List getAccounts();
}
