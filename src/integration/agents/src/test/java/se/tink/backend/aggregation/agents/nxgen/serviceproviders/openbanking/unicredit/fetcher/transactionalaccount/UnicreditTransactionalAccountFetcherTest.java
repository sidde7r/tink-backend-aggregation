package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount;

import static org.mockito.BDDMockito.given;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class UnicreditTransactionalAccountFetcherTest {

    private UnicreditTransactionalAccountFetcher accountFetcher;
    private UnicreditBaseApiClient apiClient;

    @Before
    public void setUp() {
        apiClient = Mockito.mock(UnicreditBaseApiClient.class);
        accountFetcher = new UnicreditTransactionalAccountFetcher(apiClient);
    }

    @Test
    public void fetchAccountWhenApiReturnsNoAccounts() {
        // given
        AccountsResponse accountsResponse = Mockito.mock(AccountsResponse.class);
        given(accountsResponse.getAccounts()).willReturn(Collections.emptyList());
        // and
        given(apiClient.fetchAccounts()).willReturn(accountsResponse);

        // when
        Collection<TransactionalAccount> result = accountFetcher.fetchAccounts();

        // then
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    public void fetchAccountsWhenApiReturnsAccount() {
        // given
        ExactCurrencyAmount balanceAmount = Mockito.mock(ExactCurrencyAmount.class);
        // and
        TransactionalAccount transactionalAccount = Mockito.mock(TransactionalAccount.class);
        // and
        AccountEntity accountEntity =
                mockAccountEntity("sample resource id", balanceAmount, transactionalAccount);
        // and
        mockAccountsResponse(accountEntity);
        mockBalancesResponse("sample resource id", balanceAmount);

        // when
        Collection<TransactionalAccount> result = accountFetcher.fetchAccounts();

        // then
        Assertions.assertThat(result).containsOnly(transactionalAccount);
    }

    private AccountEntity mockAccountEntity(
            final String resourceId,
            final ExactCurrencyAmount balanceAmount,
            final TransactionalAccount transactionalAccount) {
        AccountEntity accountEntity = Mockito.mock(AccountEntity.class);
        given(accountEntity.getResourceId()).willReturn(resourceId);
        given(accountEntity.toTinkAccount(balanceAmount))
                .willReturn(Optional.of(transactionalAccount));
        return accountEntity;
    }

    private AccountsResponse mockAccountsResponse(final AccountEntity accountEntity) {
        AccountsResponse accountsResponse = Mockito.mock(AccountsResponse.class);
        given(accountsResponse.getAccounts()).willReturn(Collections.singletonList(accountEntity));
        given(apiClient.fetchAccounts()).willReturn(accountsResponse);
        return accountsResponse;
    }

    private BalancesResponse mockBalancesResponse(
            final String resourceId, final ExactCurrencyAmount balanceAmount) {
        BalancesResponse balancesResponse = Mockito.mock(BalancesResponse.class);
        given(balancesResponse.getBalance()).willReturn(balanceAmount);
        given(apiClient.fetchAccountBalance(resourceId)).willReturn(balancesResponse);
        return balancesResponse;
    }
}
