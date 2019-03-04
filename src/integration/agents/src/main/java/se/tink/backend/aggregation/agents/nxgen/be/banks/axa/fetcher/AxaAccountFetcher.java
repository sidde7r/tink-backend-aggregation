package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher;

import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

import java.util.Collection;
import java.util.Collections;

public final class AxaAccountFetcher implements AccountFetcher<TransactionalAccount> {
    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return Collections.emptySet();
    }
}
