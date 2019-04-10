package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class GetAccountsResponseTest {

    @Test
    public void parseGetAccountsResponse() throws Exception {
        GetAccountsResponse getAccountsResponse = GetAccountsResponseTestData.getTestData();

        assertNotNull(getAccountsResponse);
        assertEquals(2, getAccountsResponse.getAccounts().size());
        assertTrue(10.0 == getAccountsResponse.getAccounts().get(0).getBalance());
        assertEquals("Basiskonto", getAccountsResponse.getAccounts().get(0).getName());

        List<TransactionalAccount> tinkAccounts = getAccountsResponse.getTinkAccounts();
        assertNotNull(tinkAccounts);
        assertEquals(2, tinkAccounts.size());
        assertTrue(10.0 == tinkAccounts.get(0).getBalance().getValue());
        assertEquals("Basiskonto", tinkAccounts.get(0).getName());
        assertEquals(AccountTypes.CHECKING, tinkAccounts.get(0).getType());
        assertTrue(0.0 == tinkAccounts.get(1).getBalance().getValue());
        assertEquals("Investeringskonto", tinkAccounts.get(1).getName());
        assertEquals(AccountTypes.CHECKING, tinkAccounts.get(1).getType());
    }
}
