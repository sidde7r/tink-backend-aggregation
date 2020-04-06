package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

public class FinTsAccountFetcher implements AccountFetcher<TransactionalAccount> {

    public FinTsAccountFetcher() {}

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        throw new NotImplementedException("Will be covered in next PR");
    }
}
