package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.account;

import java.math.BigDecimal;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.BancoBpiClientApi;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAccountsContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.TransactionalAccountBaseInfo;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BancoBpiTransactionalAccountFetcherTest {

    private BancoBpiEntityManager entityManager;
    private BancoBpiClientApi clientApi;

    @Before
    public void init() {
        clientApi = Mockito.mock(BancoBpiClientApi.class);
        entityManager = Mockito.mock(BancoBpiEntityManager.class);
    }

    @Test
    public void fetchAccountsShouldFetchAccounts() throws RequestException {
        // given
        BancoBpiAccountsContext accountsInfo = new BancoBpiAccountsContext();
        TransactionalAccountBaseInfo accountBaseInfo =
                Mockito.mock(TransactionalAccountBaseInfo.class);
        Mockito.when(accountBaseInfo.getInternalAccountId()).thenReturn("internalAccountId");
        Mockito.when(accountBaseInfo.getCurrency()).thenReturn("EUR");
        Mockito.when(accountBaseInfo.getAccountName()).thenReturn("Accountname");
        Mockito.when(accountBaseInfo.getIban()).thenReturn("PT50000000000000000000");
        accountsInfo.getAccountInfo().add(accountBaseInfo);
        Mockito.when(entityManager.getAccountsContext()).thenReturn(accountsInfo);
        Mockito.when(clientApi.fetchAccountBalance(accountBaseInfo))
                .thenReturn(new BigDecimal("321.74"));
        // when
        Collection<TransactionalAccount> result =
                new BancoBpiTransactionalAccountFetcher(clientApi, entityManager).fetchAccounts();
        // then
        Assert.assertFalse(result.isEmpty());
        TransactionalAccount transactionalAccount = result.iterator().next();
        Assert.assertEquals(accountBaseInfo.getAccountName(), transactionalAccount.getName());
        Assert.assertEquals(
                accountBaseInfo.getCurrency(),
                transactionalAccount.getExactBalance().getCurrencyCode());
        Assert.assertEquals(
                new BigDecimal("321.74"), transactionalAccount.getExactBalance().getExactValue());
        Assert.assertEquals(
                accountBaseInfo.getAccountName(), transactionalAccount.getAccountNumber());
        Assert.assertEquals(
                accountBaseInfo.getIban(), transactionalAccount.getIdModule().getUniqueId());
    }
}
