package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasApiBaseClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration.BnpParibasConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils.BnpParibasSignatureHeaderProvider;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BnpParibasTransactionalAccountFetcherTest {

    private BnpParibasApiBaseClient apiClient;
    private SessionStorage sessionStorage;
    private BnpParibasSignatureHeaderProvider bnpParibasSignatureHeaderProvider;
    private String token;
    private BnpParibasConfiguration bnpParibasConfiguration;
    private String signature;
    private AccountsResponse accountsResponse;

    @Before
    public void init() {
        apiClient = mock(BnpParibasApiBaseClient.class);
        sessionStorage = mock(SessionStorage.class);
        bnpParibasSignatureHeaderProvider = mock(BnpParibasSignatureHeaderProvider.class);
        token = "token";
        bnpParibasConfiguration = mock(BnpParibasConfiguration.class);
        signature = "signature";
        accountsResponse = mock(AccountsResponse.class);

        when(sessionStorage.get(BnpParibasBaseConstants.StorageKeys.TOKEN)).thenReturn(token);
        when(apiClient.getBnpParibasConfiguration()).thenReturn(bnpParibasConfiguration);
        when(bnpParibasSignatureHeaderProvider.buildSignatureHeader(
                        any(), any(), any(), any(), any()))
                .thenReturn(signature);
    }

    @Test
    public void shouldReturnEmptyCollectionOnFetchAccounts() {
        // given
        when(accountsResponse.getAccounts()).thenReturn(null);
        when(apiClient.fetchAccounts(anyString(), anyString())).thenReturn(accountsResponse);

        BnpParibasTransactionalAccountFetcher bnpParibasTransactionalAccountFetcher =
                new BnpParibasTransactionalAccountFetcher(
                        apiClient, sessionStorage, bnpParibasSignatureHeaderProvider);

        // when
        Collection<TransactionalAccount> resp =
                bnpParibasTransactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(resp);
        assertEquals(0, resp.size());
    }

    @Test
    public void shouldReturnObjectOnFetchAccounts() {
        // given
        AccountsItemEntity accountsItemEntity = mock(AccountsItemEntity.class);
        List<AccountsItemEntity> list = new ArrayList<>();
        list.add(accountsItemEntity);
        TransactionalAccount transactionalAccount = mock(TransactionalAccount.class);
        Optional<TransactionalAccount> optionalTransactionalAccount =
                Optional.ofNullable(transactionalAccount);

        BalanceResponse balanceResponse = mock(BalanceResponse.class);

        when(accountsItemEntity.toTinkAccount(any())).thenReturn(optionalTransactionalAccount);
        when(apiClient.getBalance(anyString(), anyString(), anyString()))
                .thenReturn(balanceResponse);
        when(accountsResponse.getAccounts()).thenReturn(list);
        when(apiClient.fetchAccounts(anyString(), anyString())).thenReturn(accountsResponse);

        BnpParibasTransactionalAccountFetcher bnpParibasTransactionalAccountFetcher =
                new BnpParibasTransactionalAccountFetcher(
                        apiClient, sessionStorage, bnpParibasSignatureHeaderProvider);

        // when
        Collection<TransactionalAccount> resp =
                bnpParibasTransactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(resp);
        assertTrue(resp.size() > 0);
        assertTrue(resp.contains(transactionalAccount));
    }
}
