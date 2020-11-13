package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.BalanceFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.CreditCardFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.PartyFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.TransactionalAccountFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.AccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public class AccountV31FetcherTest {

    private AccountV31Fetcher accountFetcher;
    private AccountMapper accountMapper;
    private PartyDataV31Fetcher partyFetcher;
    private UkOpenBankingApiClient apiClient;

    @Before
    public void setUp() {
        Account mockedAccount = mock(Account.class);
        UkOpenBankingAisConfig aisConfig = mock(UkOpenBankingAisConfig.class);
        when(aisConfig.getAllowedAccountOwnershipType()).thenReturn(AccountOwnershipType.PERSONAL);
        accountMapper = mock(AccountMapper.class);
        when(accountMapper.map(any(), anyCollection(), anyCollection()))
                .thenReturn(Optional.of(mockedAccount));
        when(accountMapper.supportsAccountType(any())).thenReturn(true);

        partyFetcher = mock(PartyDataV31Fetcher.class);
        apiClient = mock(UkOpenBankingApiClient.class);

        accountFetcher =
                new AccountV31Fetcher(
                        apiClient, partyFetcher, new AccountTypeMapper(aisConfig), accountMapper);
    }

    @Test
    public void returnOnlyAccountsSupportedByMapper() {
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
    }

    @Test
    @Ignore
    public void allFetchedDataIsPassedToMapper() {
        // given
        AccountEntity account = TransactionalAccountFixtures.savingsAccount();
        AccountBalanceEntity balance = BalanceFixtures.balanceCredit();
        List<IdentityDataV31Entity> parties = PartyFixtures.parties();

        // when
        when(apiClient.fetchV31Accounts()).thenReturn(ImmutableList.of(account));
        when(apiClient.fetchV31AccountBalances(account.getAccountId()))
                .thenReturn(ImmutableList.of(balance));
        when(partyFetcher.fetchAccountParties(account)).thenReturn(parties);

        accountFetcher.fetchAccounts();
        // then
        verify(accountMapper).map(account, ImmutableList.of(balance), parties);
    }
}
