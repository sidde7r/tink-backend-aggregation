package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountHolderType;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.CreditCardFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.PartyFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.TransactionalAccountFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountMapper;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.instrumentation.FetcherInstrumentationRegistry;

public class AccountV31FetcherTest {

    private AccountV31Fetcher accountFetcher;
    private AccountMapper accountMapper;
    private PartyV31Fetcher partyFetcher;
    private UkOpenBankingApiClient apiClient;
    private FetcherInstrumentationRegistry instrumentation;
    private AccountBalanceEntity balance;
    private List<PartyV31Entity> parties;

    @Before
    public void setUp() throws Exception {
        Account mockedAccount = mock(Account.class);
        accountMapper = mock(AccountMapper.class);
        when(accountMapper.map(any(), anyCollection(), anyCollection()))
                .thenReturn(Optional.of(mockedAccount));
        when(accountMapper.supportsAccountType(any())).thenReturn(true);

        partyFetcher = mock(PartyV31Fetcher.class);
        apiClient = mock(UkOpenBankingApiClient.class);
        instrumentation = new FetcherInstrumentationRegistry();

        balance = BalanceFixtures.balanceCredit();
        parties = PartyFixtures.parties();

        accountFetcher =
                new AccountV31Fetcher(apiClient, partyFetcher, accountMapper, instrumentation);
    }

    @Test
    public void shouldReturnOnlyAccountsSupportedByMapper() {
        // when
        when(apiClient.fetchV31Accounts())
                .thenReturn(
                        ImmutableList.of(
                                TransactionalAccountFixtures.savingsAccount(),
                                TransactionalAccountFixtures.currentAccount(),
                                TransactionalAccountFixtures.currentAccountBusiness(),
                                CreditCardFixtures.creditCardAccount()));

        when(accountMapper.supportsAccountType(AccountTypes.SAVINGS)).thenReturn(true);
        when(accountMapper.supportsAccountType(AccountTypes.CHECKING)).thenReturn(false);
        when(accountMapper.supportsAccountType(AccountTypes.CREDIT_CARD)).thenReturn(true);
        when(apiClient.fetchV31AccountBalances(any())).thenReturn(ImmutableList.of(balance));
        when(partyFetcher.fetchAccountParties(any())).thenReturn(parties);
        Collection<Account> result = accountFetcher.fetchAccounts();

        // then
        verify(accountMapper, times(1))
                .map(
                        eq(TransactionalAccountFixtures.savingsAccount()),
                        anyCollection(),
                        anyCollection());
        verify(accountMapper, times(1))
                .map(eq(CreditCardFixtures.creditCardAccount()), anyCollection(), anyCollection());
        assertThat(result).hasSize(2);

        // even though CHECKING is not asked for, the instrumentation will pick it up
        // further, even BUSINESS CHECKING, which is not asked for, is picked up by instrumentation
        assertEquals(
                1,
                instrumentation.getNumberAccountsSeen(
                        AccountHolderType.PERSONAL, AccountTypes.CHECKING));
        assertEquals(
                1,
                instrumentation.getNumberAccountsSeen(
                        AccountHolderType.PERSONAL, AccountTypes.SAVINGS));
        assertEquals(
                1,
                instrumentation.getNumberAccountsSeen(
                        AccountHolderType.PERSONAL, AccountTypes.CREDIT_CARD));
        assertEquals(
                1,
                instrumentation.getNumberAccountsSeen(
                        AccountHolderType.BUSINESS, AccountTypes.CHECKING));
        assertEquals(
                0,
                instrumentation.getNumberAccountsSeen(
                        AccountHolderType.BUSINESS, AccountTypes.SAVINGS));
        assertEquals(
                0,
                instrumentation.getNumberAccountsSeen(
                        AccountHolderType.BUSINESS, AccountTypes.CREDIT_CARD));
    }

    @Test
    public void shouldReturnOnlyAccountsSupportedByMapperEvenIfBusinessAccountsAreReturned() {
        // when
        when(apiClient.fetchV31Accounts())
                .thenReturn(
                        ImmutableList.of(
                                TransactionalAccountFixtures.savingsAccount(),
                                TransactionalAccountFixtures.currentAccount(),
                                CreditCardFixtures.creditCardAccount()));

        when(accountMapper.supportsAccountType(AccountTypes.SAVINGS)).thenReturn(true);
        when(accountMapper.supportsAccountType(AccountTypes.CHECKING)).thenReturn(false);
        when(accountMapper.supportsAccountType(AccountTypes.CREDIT_CARD)).thenReturn(true);
        when(apiClient.fetchV31AccountBalances(any())).thenReturn(ImmutableList.of(balance));
        when(partyFetcher.fetchAccountParties(any())).thenReturn(parties);

        Collection<Account> result = accountFetcher.fetchAccounts();

        // then
        verify(accountMapper, times(1))
                .map(
                        eq(TransactionalAccountFixtures.savingsAccount()),
                        anyCollection(),
                        anyCollection());
        verify(accountMapper, times(1))
                .map(eq(CreditCardFixtures.creditCardAccount()), anyCollection(), anyCollection());
        assertThat(result).hasSize(2);

        // even though CHECKING is not asked for, the instrumentation will pick it up
        assertEquals(
                1,
                instrumentation.getNumberAccountsSeen(
                        AccountHolderType.PERSONAL, AccountTypes.CHECKING));
        assertEquals(
                1,
                instrumentation.getNumberAccountsSeen(
                        AccountHolderType.PERSONAL, AccountTypes.SAVINGS));
        assertEquals(
                1,
                instrumentation.getNumberAccountsSeen(
                        AccountHolderType.PERSONAL, AccountTypes.CREDIT_CARD));
        assertEquals(
                0,
                instrumentation.getNumberAccountsSeen(
                        AccountHolderType.BUSINESS, AccountTypes.CHECKING));
        assertEquals(
                0,
                instrumentation.getNumberAccountsSeen(
                        AccountHolderType.BUSINESS, AccountTypes.SAVINGS));
        assertEquals(
                0,
                instrumentation.getNumberAccountsSeen(
                        AccountHolderType.BUSINESS, AccountTypes.CREDIT_CARD));
    }

    @Test
    public void shouldReturnPersonalAndBusinessAccountsToTheMapper() {
        // when
        when(apiClient.fetchV31Accounts())
                .thenReturn(
                        ImmutableList.of(
                                TransactionalAccountFixtures.currentAccount(),
                                TransactionalAccountFixtures.currentAccountBusiness()));

        when(accountMapper.supportsAccountType(AccountTypes.CHECKING)).thenReturn(true);
        when(apiClient.fetchV31AccountBalances(any())).thenReturn(ImmutableList.of(balance));
        when(partyFetcher.fetchAccountParties(any())).thenReturn(parties);
        Collection<Account> result = accountFetcher.fetchAccounts();

        // then
        verify(accountMapper, times(1))
                .map(
                        eq(TransactionalAccountFixtures.currentAccount()),
                        anyCollection(),
                        anyCollection());
        verify(accountMapper, times(1))
                .map(
                        eq(TransactionalAccountFixtures.currentAccountBusiness()),
                        anyCollection(),
                        anyCollection());
        assertThat(result).hasSize(2);

        // even though CHECKING is not asked for, the instrumentation will pick it up
        assertEquals(
                1,
                instrumentation.getNumberAccountsSeen(
                        AccountHolderType.PERSONAL, AccountTypes.CHECKING));
        assertEquals(
                0,
                instrumentation.getNumberAccountsSeen(
                        AccountHolderType.PERSONAL, AccountTypes.SAVINGS));
        assertEquals(
                0,
                instrumentation.getNumberAccountsSeen(
                        AccountHolderType.PERSONAL, AccountTypes.CREDIT_CARD));
        assertEquals(
                1,
                instrumentation.getNumberAccountsSeen(
                        AccountHolderType.BUSINESS, AccountTypes.CHECKING));
        assertEquals(
                0,
                instrumentation.getNumberAccountsSeen(
                        AccountHolderType.BUSINESS, AccountTypes.SAVINGS));
        assertEquals(
                0,
                instrumentation.getNumberAccountsSeen(
                        AccountHolderType.BUSINESS, AccountTypes.CREDIT_CARD));
    }

    @Test
    public void shouldAllFetchedDataPassToMapper() {
        // given
        AccountEntity account = TransactionalAccountFixtures.savingsAccount();

        // when
        when(apiClient.fetchV31Accounts()).thenReturn(ImmutableList.of(account));
        when(apiClient.fetchV31AccountBalances(account.getAccountId()))
                .thenReturn(ImmutableList.of(balance));
        when(partyFetcher.fetchAccountParties(account)).thenReturn(parties);

        accountFetcher.fetchAccounts();

        // then
        verify(accountMapper).map(account, ImmutableList.of(balance), parties);
    }

    @Test
    public void shouldReturnEmptyAccountList() {
        // given
        AccountEntity account = TransactionalAccountFixtures.currentAccountWithEmptyAccountId();
        when(apiClient.fetchV31Accounts()).thenReturn(ImmutableList.of(account));

        // when
        Collection<AccountEntity> accounts = accountFetcher.fetchAccounts();

        // then
        assertThat(accounts).isEmpty();
    }

    @Test
    public void shouldReturnEmptyAccountListWhenFetchedBalanceListIsEmpty() {
        // given
        AccountEntity account = TransactionalAccountFixtures.savingsAccount();

        // when
        when(apiClient.fetchV31Accounts()).thenReturn(ImmutableList.of(account));
        when(apiClient.fetchV31AccountBalances(any())).thenReturn(Collections.emptyList());
        when(partyFetcher.fetchAccountParties(any())).thenReturn(parties);

        Collection<AccountEntity> accounts = accountFetcher.fetchAccounts();

        // then
        assertThat(accounts).isEmpty();
    }

    @Test
    public void shouldReturn3Accounts() {
        // given
        List<AccountEntity> accounts =
                ImmutableList.of(
                        TransactionalAccountFixtures.savingsAccount(),
                        TransactionalAccountFixtures.currentAccount(),
                        TransactionalAccountFixtures.currentAccountBusiness());

        // when
        when(apiClient.fetchV31Accounts()).thenReturn(accounts);
        when(partyFetcher.fetchAccountParties(any())).thenReturn(parties);
        when(apiClient.fetchV31AccountBalances(any())).thenReturn(ImmutableList.of(balance));

        Collection<AccountEntity> accountEntities = accountFetcher.fetchAccounts();

        // then
        assertThat(accountEntities).hasSize(3);
    }
}
