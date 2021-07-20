package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.common.types.CashAccountType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.dto.AccountResourceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class CmcicTransactionalAccountFetcherTest {

    private CmcicApiClient apiClient;
    private CmcicTransactionalAccountFetcher cmcicTransactionalAccountFetcher;
    private FetchAccountsResponse fetchAccountsResponse;
    private AccountResourceDto accountResourceDto;
    private TransactionalAccount transactionalAccount;

    @Before
    public void init() {
        apiClient = mock(CmcicApiClient.class);
        fetchAccountsResponse = mock(FetchAccountsResponse.class);
        accountResourceDto = mock(AccountResourceDto.class);
        transactionalAccount = mock(TransactionalAccount.class);

        final CmcicTransactionalAccountConverter transactionalAccountConverterMock =
                mock(CmcicTransactionalAccountConverter.class);

        cmcicTransactionalAccountFetcher =
                new CmcicTransactionalAccountFetcher(apiClient, transactionalAccountConverterMock);

        when(transactionalAccountConverterMock.convertToAccount(accountResourceDto))
                .thenReturn(Optional.of(transactionalAccount));
    }

    @Test
    public void shouldFetchAccounts() {
        // given
        List<AccountResourceDto> accountsList = ImmutableList.of(accountResourceDto);
        when(accountResourceDto.getCashAccountType()).thenReturn(CashAccountType.CACC);

        when(apiClient.fetchAccounts()).thenReturn(fetchAccountsResponse);
        when(fetchAccountsResponse.getAccounts()).thenReturn(accountsList);

        // when
        Collection<TransactionalAccount> response =
                cmcicTransactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertTrue(response.contains(transactionalAccount));
    }

    @Test
    public void shouldReturnEmptyCollectionOnFetchAccounts() {
        // given
        when(apiClient.fetchAccounts()).thenReturn(fetchAccountsResponse);
        when(fetchAccountsResponse.getAccounts()).thenReturn(Collections.emptyList());

        // when
        Collection<TransactionalAccount> response =
                cmcicTransactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(response);
        assertEquals(0, response.size());
    }
}
