package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenTest;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import static org.junit.Assert.assertFalse;

public class AlandsBankenAccountFetcherTest extends AlandsBankenTest {

    @Test
    public void fetchesAccounts() throws Exception {
        Collection<TransactionalAccount> accounts = new AlandsBankenTransactionalAccountFetcher(client).fetchAccounts();

        assertFalse(accounts.isEmpty());
        accounts.forEach(
                account -> assertFalse(account.getIdentifiers().isEmpty())
        );

    }
}
