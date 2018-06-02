package se.tink.backend.aggregation.nxgen.controllers.refresh;

import java.util.Collection;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public interface AccountFetcher<T extends Account> {
    Collection<T> fetchAccounts();
}
