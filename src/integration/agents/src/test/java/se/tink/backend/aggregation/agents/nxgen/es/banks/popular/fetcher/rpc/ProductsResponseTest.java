package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import static org.junit.Assert.assertNotNull;

public class ProductsResponseTest {
    @Test
    public void toTinkAccounts() throws Exception {
        FetchAccountsResponse response = FetchAccountsResponseTestData.getTestData();
        Collection<TransactionalAccount> accounts = response.getTinkAccounts();

        assertNotNull(accounts);
        for (TransactionalAccount account : accounts) {
            System.out.println(String.format("ACCOUNT %s  %.2f", account.getAccountNumber(), account.getBalance().getValue()));
            assertNotNull(account.getAccountNumber());
            assertNotNull(account.getBankIdentifier());
            assertNotNull(account.getBalance());
        }
    }
}
