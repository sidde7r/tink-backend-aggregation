package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.MediolanumApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.data.AccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class MediolanumAccountFetcherTest {

    private MediolanumApiClient mockApiClient;
    private AccountMapper mockAccountMapper;

    private MediolanumAccountFetcher accountFetcher;

    @Before
    public void setup() {
        mockApiClient = mock(MediolanumApiClient.class);
        mockAccountMapper = mock(AccountMapper.class);

        when(mockAccountMapper.toTinkAccount(any()))
                .thenReturn(Optional.of(mock(TransactionalAccount.class)));

        accountFetcher = new MediolanumAccountFetcher(mockApiClient, mockAccountMapper);
    }

    @Test
    public void shouldFetchAccountsAndMapThem() {
        // given
        when(mockApiClient.fetchAccounts())
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.TWO_ACCOUNTS, AccountsResponse.class));
        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        // then
        assertThat(accounts).hasSize(2);
        verify(mockApiClient).fetchAccounts();
        verify(mockAccountMapper, times(2)).toTinkAccount(any());
    }

    @Test
    public void shouldHandleEmptyResponseProperly() {
        // given
        when(mockApiClient.fetchAccounts()).thenReturn(new AccountsResponse());

        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        // then
        assertThat(accounts).hasSize(0);
        verify(mockApiClient).fetchAccounts();
        verifyNoMoreInteractions(mockAccountMapper);
    }
}
