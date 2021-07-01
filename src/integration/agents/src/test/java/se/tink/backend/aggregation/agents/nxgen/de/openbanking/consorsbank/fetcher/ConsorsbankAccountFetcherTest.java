package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.ConsorsbankStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.client.ConsorsbankFetcherApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.mappers.AccountMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class ConsorsbankAccountFetcherTest {

    private static final String TEST_CONSENT_ID = "test_consent_id";
    private static final String TEST_URL_BALANCES =
            "https://xs2a.consorsbank.de/v1/accounts/asdf1234/balances";

    private ConsorsbankFetcherApiClient mockApiClient;
    private ConsorsbankStorage mockStorage;
    private AccountMapper mockAccountMapper;

    private ConsorsbankAccountFetcher accountFetcher;

    @Before
    public void setup() {
        mockApiClient = mock(ConsorsbankFetcherApiClient.class);
        mockStorage = mock(ConsorsbankStorage.class);
        mockAccountMapper = mock(ConsorsbankAccountMapper.class);

        when(mockStorage.getConsentId()).thenReturn(TEST_CONSENT_ID);
        when(mockAccountMapper.toTinkAccount(any())).thenReturn(Optional.empty());

        accountFetcher =
                new ConsorsbankAccountFetcher(mockApiClient, mockStorage, mockAccountMapper);
    }

    @Test
    public void shouldFetchWithBalancesInOneRequestWhenAllAccountsWithBalanceAccess() {
        // given
        when(mockStorage.getConsentAccess()).thenReturn(accessEntityWithAllBalancesAllowed());
        when(mockApiClient.fetchAccounts(TEST_CONSENT_ID, true))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.TWO_ACCOUNTS, FetchAccountsResponse.class));

        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        // then
        assertThat(accounts).isEmpty();
        verify(mockAccountMapper, times(2)).toTinkAccount(any());
        verify(mockApiClient).fetchAccounts(TEST_CONSENT_ID, true);
    }

    @Test
    public void shouldFetchWithBalancesInSeparateRequestsWhenNotAllAccountsAllowedBalances() {
        // given
        when(mockStorage.getConsentAccess()).thenReturn(accessEntityWithSomeBalancesAllowed());
        when(mockApiClient.fetchAccounts(TEST_CONSENT_ID, false))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.TWO_ACCOUNTS, FetchAccountsResponse.class));
        when(mockApiClient.fetchBalances(TEST_CONSENT_ID, TEST_URL_BALANCES))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.BALANCES, FetchBalancesResponse.class));

        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        // then
        assertThat(accounts).isEmpty();
        verify(mockAccountMapper, times(2)).toTinkAccount(any());
        verify(mockApiClient).fetchAccounts(TEST_CONSENT_ID, false);
        verify(mockApiClient).fetchBalances(TEST_CONSENT_ID, TEST_URL_BALANCES);
    }

    private AccessEntity accessEntityWithAllBalancesAllowed() {

        AccountReferenceEntity first = new AccountReferenceEntity("DE1234");
        AccountReferenceEntity second = new AccountReferenceEntity("DE4321");

        List<AccountReferenceEntity> both = new ArrayList<>();
        both.add(first);
        both.add(second);
        return AccessEntity.builder().accounts(both).balances(both).build();
    }

    private AccessEntity accessEntityWithSomeBalancesAllowed() {
        AccountReferenceEntity first = new AccountReferenceEntity("DE1234");
        AccountReferenceEntity second = new AccountReferenceEntity("DE4321");

        List<AccountReferenceEntity> accounts = new ArrayList<>();
        accounts.add(first);
        accounts.add(second);
        List<AccountReferenceEntity> balances = new ArrayList<>();
        balances.add(first);
        return AccessEntity.builder().accounts(accounts).balances(balances).build();
    }
}
