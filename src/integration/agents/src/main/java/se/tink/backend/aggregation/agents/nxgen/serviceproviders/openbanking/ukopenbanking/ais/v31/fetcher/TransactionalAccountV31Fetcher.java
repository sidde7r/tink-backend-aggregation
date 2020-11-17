package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class TransactionalAccountV31Fetcher implements AccountFetcher<TransactionalAccount> {

    private final AccountV31Fetcher<TransactionalAccount> accountV31Fetcher;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return accountV31Fetcher.fetchAccounts();
    }
}
