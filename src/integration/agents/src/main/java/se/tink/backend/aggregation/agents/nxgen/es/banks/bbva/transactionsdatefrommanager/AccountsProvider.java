package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.transactionsdatefrommanager;

import java.util.Collection;
import se.tink.backend.aggregation.nxgen.core.account.Account;

@FunctionalInterface
public interface AccountsProvider {
    Collection<? extends Account> getAccounts();
}
