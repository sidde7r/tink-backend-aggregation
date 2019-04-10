package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse.SdcSeConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class FilterAccountsResponseTest {
    private SdcConfiguration agentConfiguration;

    @Before
    public void setUp() throws Exception {
        Provider provider = new Provider();
        provider.setMarket("SE");
        provider.setPayload("9750");
        agentConfiguration = new SdcSeConfiguration(provider);
    }

    @Test
    public void getTinkAccounts() throws Exception {
        FilterAccountsResponse response = FilterAccountsResponseTestData.getTestData();
        assertTrue(response.size() == 2);

        Collection<TransactionalAccount> accounts = response.getTinkAccounts(agentConfiguration);

        assertNotNull(accounts);
        assertTrue(accounts.size() == 1);
        for (TransactionalAccount account : accounts) {
            assertNotNull(account.getName());
            assertNotNull(account.getBankIdentifier());
            assertNotNull(account.getAccountNumber());
            assertTrue(account.getBalance().getValue() != 0);
            System.out.println("Account: " + account.toString() + ", TYPE: " + account.getType());
        }
    }
}
