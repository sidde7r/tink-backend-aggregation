package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher;

import static org.junit.Assert.assertFalse;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenFIConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.CrossKeyTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class AlandsBankenAccountFetcherTest extends AlandsBankenTest {

    @Test
    public void fetchesAccounts() throws Exception {
        Collection<TransactionalAccount> accounts =
                new CrossKeyTransactionalAccountFetcher(client, new AlandsBankenFIConfiguration())
                        .fetchAccounts();

        assertFalse(accounts.isEmpty());
        accounts.forEach(account -> assertFalse(account.getIdentifiers().isEmpty()));
    }
}
