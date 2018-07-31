package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenTest;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import static org.junit.Assert.assertNotNull;

public class AlandsBankenTransactionFetcherTest extends AlandsBankenTest {

    @Test
    public void canFetchTransactions() {
        Collection<TransactionalAccount> accounts = new AlandsBankenTransactionalAccountFetcher(client).fetchAccounts();
        accounts.forEach( account ->
            new AlandsBankenTransactionFetcher(client).getTransactionsFor(account, null, null)
                    .getTinkTransactions()
                .forEach(transaction -> {
                    assertNotNull(transaction);
                    assertNotNull(transaction.getDescription());
                })
        );
    }
}
