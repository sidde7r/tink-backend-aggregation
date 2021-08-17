package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class UnicreditTransactionalAccountFetcherTest {

    private UnicreditTransactionalAccountFetcher accountFetcher;
    private UnicreditBaseApiClient apiClient;
    private UnicreditTransactionalAccountMapper mapper;

    @Before
    public void setUp() {
        apiClient = mock(UnicreditBaseApiClient.class);
        mapper = mock(UnicreditTransactionalAccountMapper.class);
        accountFetcher = new UnicreditTransactionalAccountFetcher(apiClient, mapper);
    }

    @Test
    public void fetchAccountWhenApiReturnsNoAccounts() {
        // given
        AccountsResponse accountsResponse = mock(AccountsResponse.class);
        given(accountsResponse.getAccounts()).willReturn(Collections.emptyList());
        // and
        given(apiClient.fetchAccounts()).willReturn(accountsResponse);

        // when
        Collection<TransactionalAccount> result = accountFetcher.fetchAccounts();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void fetchAccountsWhenApiReturnsAccount() {
        // given
        AccountEntity accountDetailsEntity = mock(AccountEntity.class);
        // and
        TransactionalAccount transactionalAccount = mock(TransactionalAccount.class);
        given(mapper.toTinkAccount(eq(accountDetailsEntity), any()))
                .willReturn(Optional.of(transactionalAccount));
        // and
        AccountEntity accountEntity = mockAccountEntity("sample resource id");
        // and
        mockAccountsResponse(accountEntity);
        mockAccountDetailsResponse("sample resource id", accountDetailsEntity);
        mockBalancesResponse("sample resource id");

        // when
        Collection<TransactionalAccount> result = accountFetcher.fetchAccounts();

        // then
        assertThat(result).containsOnly(transactionalAccount);
    }

    private AccountEntity mockAccountEntity(final String resourceId) {
        AccountEntity accountEntity = mock(AccountEntity.class);
        given(accountEntity.getResourceId()).willReturn(resourceId);
        return accountEntity;
    }

    private AccountsResponse mockAccountsResponse(final AccountEntity accountEntity) {
        AccountsResponse accountsResponse = mock(AccountsResponse.class);
        given(accountsResponse.getAccounts()).willReturn(Collections.singletonList(accountEntity));
        given(apiClient.fetchAccounts()).willReturn(accountsResponse);
        return accountsResponse;
    }

    private AccountDetailsResponse mockAccountDetailsResponse(
            final String resourceId, final AccountEntity accountDetailsEntity) {
        AccountDetailsResponse accountDetailsResponse = mock(AccountDetailsResponse.class);
        given(accountDetailsResponse.getAccount()).willReturn(accountDetailsEntity);
        given(apiClient.fetchAccountDetails(resourceId)).willReturn(accountDetailsResponse);
        return accountDetailsResponse;
    }

    private BalancesResponse mockBalancesResponse(final String resourceId) {
        BalancesResponse balancesResponse = mock(BalancesResponse.class);
        given(balancesResponse.getBalances()).willReturn(Collections.emptyList());
        given(apiClient.fetchAccountBalance(resourceId)).willReturn(balancesResponse);
        return balancesResponse;
    }
}
