package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount;

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
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration.SocieteGeneraleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils.SignatureHeaderProvider;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SocieteGeneraleTransactionalAccountFetcherTest {

    private SocieteGeneraleApiClient apiClient;
    private SocieteGeneraleConfiguration configuration;
    private SessionStorage sessionStorage;
    private SignatureHeaderProvider signatureHeaderProvider;
    private EndUserIdentityResponse user;
    private AccountsResponse accountsResponse;
    private EidasProxyConfiguration eidasProxyConfiguration;
    private EidasIdentity eidasIdentity;
    String signature = "signature";
    String token = "token";
    String connectedPsu = "connectedPsu";

    @Before
    public void init() {
        apiClient = mock(SocieteGeneraleApiClient.class);
        configuration = mock(SocieteGeneraleConfiguration.class);
        sessionStorage = mock(SessionStorage.class);
        signatureHeaderProvider = mock(SignatureHeaderProvider.class);
        user = mock(EndUserIdentityResponse.class);
        accountsResponse = mock(AccountsResponse.class);
        eidasProxyConfiguration = mock(EidasProxyConfiguration.class);
        eidasIdentity = mock(EidasIdentity.class);

        when(sessionStorage.get(SocieteGeneraleConstants.StorageKeys.TOKEN)).thenReturn(token);
        when(signatureHeaderProvider.buildSignatureHeader(
                        any(), any(), anyString(), anyString(), any()))
                .thenReturn(signature);
        when(apiClient.getEndUserIdentity(anyString(), anyString())).thenReturn(user);
        when(user.getConnectedPsu()).thenReturn(connectedPsu);
    }

    @Test
    public void shouldReturnProperAccount() {
        // given
        AccountsItemEntity accountsItemEntity = mock(AccountsItemEntity.class);
        List<AccountsItemEntity> cashAccounts = new ArrayList<>();
        cashAccounts.add(accountsItemEntity);
        TransactionalAccount transactionalAccount = mock(TransactionalAccount.class);
        Optional<TransactionalAccount> optionalTransactionalAccount =
                Optional.ofNullable(transactionalAccount);

        when(accountsItemEntity.toTinkModel(any())).thenReturn(optionalTransactionalAccount);
        when(accountsResponse.getCashAccounts()).thenReturn(cashAccounts);
        when(apiClient.fetchAccounts(anyString(), anyString())).thenReturn(accountsResponse);

        SocieteGeneraleTransactionalAccountFetcher societeGeneraleTransactionalAccountFetcher =
                new SocieteGeneraleTransactionalAccountFetcher(
                        apiClient,
                        configuration,
                        sessionStorage,
                        signatureHeaderProvider,
                        eidasProxyConfiguration,
                        eidasIdentity);

        // when
        Collection<TransactionalAccount> accounts =
                societeGeneraleTransactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(accounts);
        assertEquals(1, accounts.size());
        assertTrue(accounts.contains(transactionalAccount));
    }

    @Test
    public void shouldReturnEmptyListOfAccountsWhenApiReturnsNullAccounts() {
        // given
        when(apiClient.fetchAccounts(anyString(), anyString())).thenReturn(null);
        SocieteGeneraleTransactionalAccountFetcher societeGeneraleTransactionalAccountFetcher =
                new SocieteGeneraleTransactionalAccountFetcher(
                        apiClient,
                        configuration,
                        sessionStorage,
                        signatureHeaderProvider,
                        eidasProxyConfiguration,
                        eidasIdentity);

        // when
        Collection<TransactionalAccount> accounts =
                societeGeneraleTransactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(accounts);
        assertEquals(0, accounts.size());
    }
}
