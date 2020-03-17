package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.BalanceFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.CreditCardFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.PartyFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.TransactionalAccountFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts.TransactionalAccountMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public class TransactionalAccountV31FetcherTest {

    private TransactionalAccountV31Fetcher accountFetcher;
    private TransactionalAccountMapper accountMapper;
    private PartyDataV31Fetcher partyDataV31Fetcher;
    private UkOpenBankingApiClient apiClient;

    @Before
    public void setUp() {
        accountMapper = mock(TransactionalAccountMapper.class);
        partyDataV31Fetcher = mock(PartyDataV31Fetcher.class);
        apiClient = mock(UkOpenBankingApiClient.class);

        accountFetcher =
                new TransactionalAccountV31Fetcher(apiClient, accountMapper, partyDataV31Fetcher);
    }

    @Test
    public void onlyCheckingAndSavingAccountsAreFetched() {
        // when
        when(apiClient.fetchV31Accounts())
                .thenReturn(
                        ImmutableList.of(
                                TransactionalAccountFixtures.savingsAccount(),
                                TransactionalAccountFixtures.currentAccount(),
                                CreditCardFixtures.creditCardAccount()));

        when(accountMapper.map(any(), any(), anyCollection(), anyCollection()))
                .thenReturn(Optional.of(mock(TransactionalAccount.class)));

        Collection<TransactionalAccount> result = accountFetcher.fetchAccounts();

        // then
        verify(accountMapper, times(2)).map(any(), any(), anyCollection(), anyCollection());
        assertThat(result).hasSize(2);
    }

    @Test
    public void allFetchedDataIsPassedToMapper() {
        // given
        AccountEntity account = TransactionalAccountFixtures.savingsAccount();
        AccountBalanceEntity balance = BalanceFixtures.balanceCredit();
        List<IdentityDataV31Entity> parties = PartyFixtures.parties();

        // when
        when(apiClient.fetchV31Accounts()).thenReturn(ImmutableList.of(account));
        when(apiClient.fetchV31AccountBalances(account.getBankIdentifier()))
                .thenReturn(ImmutableList.of(balance));
        when(partyDataV31Fetcher.fetchAccountParties(account.getBankIdentifier()))
                .thenReturn(parties);

        accountFetcher.fetchAccounts();
        // then
        verify(accountMapper)
                .map(account, TransactionalAccountType.SAVINGS, ImmutableList.of(balance), parties);
    }
}
