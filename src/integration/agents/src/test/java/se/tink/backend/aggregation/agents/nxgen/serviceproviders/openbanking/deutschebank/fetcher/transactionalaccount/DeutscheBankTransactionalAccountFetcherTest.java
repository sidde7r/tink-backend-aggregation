package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType.CHECKING;
import static se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType.SAVINGS;

import java.util.Collection;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.account.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.account.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.transactions.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class DeutscheBankTransactionalAccountFetcherTest {

    private DeutscheBankApiClient apiClient = mock(DeutscheBankApiClient.class);
    private static final FetchBalancesResponse BALANCE_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"balances\" : [ { \"balanceType\" : \"closingBooked\", \"balanceAmount\" : {\"currency\" : \"EUR\", \"amount\" : 12.12 }}, { \"balanceType\" : \"expected\", \"balanceAmount\" : {\"currency\" : \"EUR\", \"amount\" : 12.12 }} ] }",
                    FetchBalancesResponse.class);

    private static final TransactionsKeyPaginatorBaseResponse TRANSACTIONS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"_links\" : {} , \"transactions\" : { \"booked\" : [{ \"valueDate\" : \"2000-10-10\", \"remittanceInformationUnstructured\" : \"DESCRIPTION\", \"transactionAmount\" : { \"amount\" : \"6.66\" , \"currency\" : \"EUR\"} }], \"pending\" : [{\"valueDate\" : \"2000-10-10\", \"remittanceInformationUnstructured\" : \"DESCRIPTION\", \"transactionAmount\" : { \"amount\" : \"6.66\" , \"currency\" : \"EUR\"}}]} }",
                    TransactionsKeyPaginatorBaseResponse.class);

    private DeutscheBankTransactionalAccountFetcher fetcher =
            new DeutscheBankTransactionalAccountFetcher(apiClient);

    @Test
    @Parameters(method = "checkingAccountKeysParameters")
    public void shouldFetchAndMapCheckingTransactionalAccount(String accountType) {
        // given
        FetchAccountsResponse fetchAccountsResponse = getAccountsResponse(accountType);
        AccountEntity accountEntity = fetchAccountsResponse.getAccounts().get(0);
        given(apiClient.fetchAccounts()).willReturn(fetchAccountsResponse);
        given(apiClient.fetchBalances(accountEntity)).willReturn(BALANCE_RESPONSE);

        // when
        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        // then
        assertThat(DeutscheBankConstants.ACCOUNT_TYPE_MAPPER.isOf(accountType, CHECKING)).isTrue();
        TransactionalAccount account = accounts.iterator().next();
        assertAccountWithType(account, CHECKING);
    }

    @Test
    @Parameters(method = "savingAccountKeysParameters")
    public void shouldFetchAndMapSavingsTransactionalAccount(String accountType) {
        // given
        FetchAccountsResponse fetchAccountsResponse = getAccountsResponse(accountType);
        AccountEntity accountEntity = fetchAccountsResponse.getAccounts().get(0);
        given(apiClient.fetchAccounts()).willReturn(fetchAccountsResponse);
        given(apiClient.fetchBalances(accountEntity)).willReturn(BALANCE_RESPONSE);

        // when
        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        // then
        assertThat(DeutscheBankConstants.ACCOUNT_TYPE_MAPPER.isOf(accountType, SAVINGS)).isTrue();
        TransactionalAccount account = accounts.iterator().next();
        assertAccountWithType(account, SAVINGS);
    }

    private void assertAccountWithType(
            TransactionalAccount account, TransactionalAccountType type) {
        assertThat(account.getAccountNumber()).isEqualTo("PL666");
        assertThat(account.getType().name()).isEqualTo(type.name());
        assertThat(account.getName()).isEqualTo("NAME");
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.inEUR(12.12));
        assertThat(account.getParties()).hasSize(1);
        assertThat(account.getParties().get(0).getName()).isEqualTo("Name Surname");
        assertThat(account.getParties().get(0).getRole()).isEqualTo(Party.Role.HOLDER);
    }

    @Test
    public void shouldFetchAndMapTransactionsToTinkModel() {
        // given
        TransactionalAccount transactionalAccount = mock(TransactionalAccount.class);
        given(apiClient.fetchTransactionsForAccount(transactionalAccount, "key"))
                .willReturn(TRANSACTIONS_RESPONSE);

        // when
        TransactionKeyPaginatorResponse<String> result =
                fetcher.getTransactionsFor(transactionalAccount, "key");

        // then
        Transaction transaction = result.getTinkTransactions().iterator().next();
        assertThat(transaction.getDescription()).isEqualTo("DESCRIPTION");
        assertThat(transaction.getDate().toString()).isEqualTo("Tue Oct 10 10:00:00 UTC 2000");
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.inEUR(6.66));
    }

    @SuppressWarnings("unused")
    private static Object checkingAccountKeysParameters() {
        return DeutscheBankConstants.CHECKING_ACCOUNT_KEYS;
    }

    @SuppressWarnings("unused")
    private static Object savingAccountKeysParameters() {
        return DeutscheBankConstants.SAVING_ACCOUNT_KEYS;
    }

    private static FetchAccountsResponse getAccountsResponse(String accountType) {
        return SerializationUtils.deserializeFromString(
                "{\"accounts\" : [{\"iban\" : \"PL666\", \"resourceId\" : \"1\", \"name\" : \"NAME\", \"ownerName\" : \"Name Surname\", \"currency\" : \"EUR\", \"product\" : \""
                        + accountType
                        + "\"}]}",
                FetchAccountsResponse.class);
    }
}
