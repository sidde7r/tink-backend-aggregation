package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static se.tink.backend.agents.rpc.AccountTypes.CHECKING;
import static se.tink.backend.agents.rpc.AccountTypes.SAVINGS;
import static se.tink.libraries.amount.ExactCurrencyAmount.of;

import java.util.Collection;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetBalanceResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class Xs2aDevelopersTransactionalAccountFetcherTest {

    private static final GetBalanceResponse BALANCE_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"balances\" : [ {\"balanceAmount\" : {\"currency\" : \"EUR\", \"amount\" : 12.12 }}, {\"balanceAmount\" : {\"currency\" : \"EUR\", \"amount\" : 12.12 }} ] }",
                    GetBalanceResponse.class);
    private Xs2aDevelopersApiClient apiClient = mock(Xs2aDevelopersApiClient.class);
    private Xs2aDevelopersTransactionalAccountFetcher accountFetcher =
            new Xs2aDevelopersTransactionalAccountFetcher(apiClient);

    @Test
    @Parameters(method = "checkingAccountKeysParameters")
    public void shouldFetchAndMapCheckingTransactionalAccount(String accountType) {
        // given
        GetAccountsResponse getAccountsResponse = getAccountsResponse(accountType);
        AccountEntity accountEntity = getAccountsResponse.getAccountList().get(0);
        given(apiClient.getAccounts()).willReturn(getAccountsResponse);
        given(apiClient.getBalance(accountEntity)).willReturn(BALANCE_RESPONSE);

        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        // then
        assertThat(Xs2aDevelopersConstants.ACCOUNT_TYPE_MAPPER.isOf(accountType, CHECKING))
                .isTrue();
        assertTransactionalAccountIsProperlyMapped(
                accounts.iterator().next(), "PL666", "NAME", of("12.12", "EUR"));
        verify(apiClient).getAccounts();
        verify(apiClient).getBalance(accountEntity);
        verifyNoMoreInteractions(apiClient);
    }

    @Test
    @Parameters(method = "savingAccountKeysParameters")
    public void shouldFetchAndMapSavingTransactionalAccount(String accountType) {
        // given
        GetAccountsResponse getAccountsResponse = getAccountsResponse(accountType);
        AccountEntity accountEntity = getAccountsResponse.getAccountList().get(0);
        given(apiClient.getAccounts()).willReturn(getAccountsResponse);
        given(apiClient.getBalance(accountEntity)).willReturn(BALANCE_RESPONSE);

        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        // then
        assertThat(Xs2aDevelopersConstants.ACCOUNT_TYPE_MAPPER.isOf(accountType, SAVINGS)).isTrue();
        assertTransactionalAccountIsProperlyMapped(
                accounts.iterator().next(), "PL666", "NAME", of("12.12", "EUR"));
        verify(apiClient).getAccounts();
        verify(apiClient).getBalance(accountEntity);
        verifyNoMoreInteractions(apiClient);
    }

    @Test
    public void shouldNotMapUnknownTypeTransactionalAccount() {
        // given
        GetAccountsResponse getAccountsResponse = getAccountsResponse("UNKNOWN_TYPE");
        AccountEntity accountEntity = getAccountsResponse.getAccountList().get(0);
        given(apiClient.getAccounts()).willReturn(getAccountsResponse);
        given(apiClient.getBalance(accountEntity)).willReturn(BALANCE_RESPONSE);

        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        // then
        assertThat(accounts).isEmpty();
    }

    @SuppressWarnings("unused")
    private static Object checkingAccountKeysParameters() {
        return Xs2aDevelopersConstants.CHECKING_ACCOUNT_KEYS;
    }

    @SuppressWarnings("unused")
    private static Object savingAccountKeysParameters() {
        return Xs2aDevelopersConstants.SAVING_ACCOUNT_KEYS;
    }

    private void assertTransactionalAccountIsProperlyMapped(
            TransactionalAccount account,
            String accountNumber,
            String name,
            ExactCurrencyAmount balance) {
        assertThat(account.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(account.getName()).isEqualTo(name);
        assertThat(account.getExactBalance()).isEqualTo(balance);
    }

    private static GetAccountsResponse getAccountsResponse(String accountType) {
        return SerializationUtils.deserializeFromString(
                "{\"accounts\" : [{\"iban\" : \"PL666\", \"resourceId\" : \"1\", \"name\" : \"NAME\", \"currency\" : \"EUR\", \"product\" : \""
                        + accountType
                        + "\"}]}",
                GetAccountsResponse.class);
    }
}
