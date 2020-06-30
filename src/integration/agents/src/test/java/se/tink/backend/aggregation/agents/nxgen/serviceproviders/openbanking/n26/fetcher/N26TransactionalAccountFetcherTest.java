package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.testdata.N26TransactionalAccountFetcherTestData.ACCOUNT_BALANCE_RESPONSE_JSON;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.testdata.N26TransactionalAccountFetcherTestData.ACCOUNT_BALANCE_RESPONSE_JSON_WITHOUT_AVAILABLE_BALANCE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.testdata.N26TransactionalAccountFetcherTestData.ACCOUNT_BALANCE_RESPONSE_JSON_WITHOUT_CURRENT_BALANCE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.testdata.N26TransactionalAccountFetcherTestData.ACCOUNT_IBAN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.testdata.N26TransactionalAccountFetcherTestData.ACCOUNT_NAME;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.testdata.N26TransactionalAccountFetcherTestData.ACCOUNT_RESPONSE_JSON;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.testdata.N26TransactionalAccountFetcherTestData.ACCOUNT_RESPONSE_JSON_WITHOUT_IBAN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.testdata.N26TransactionalAccountFetcherTestData.AVAILABLE_BALANCE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.testdata.N26TransactionalAccountFetcherTestData.CURRENCY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.testdata.N26TransactionalAccountFetcherTestData.CURRENT_BALANCE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.testdata.N26TransactionalAccountFetcherTestData.EMPTY_ACCOUNT_RESPONSE_JSON;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.testdata.N26TransactionalAccountFetcherTestData.EMPTY_TRANSACTIONS_RESPONSE_JSON;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.testdata.N26TransactionalAccountFetcherTestData.OFFSET_1;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.testdata.N26TransactionalAccountFetcherTestData.OFFSET_2;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.testdata.N26TransactionalAccountFetcherTestData.RESOURCE_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.testdata.N26TransactionalAccountFetcherTestData.TRANSACTIONS_RESPONSE_JSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.exceptions.refresh.CheckingAccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class N26TransactionalAccountFetcherTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private N26TransactionalAccountFetcher transactionalAccountFetcher;
    private N26ApiClient apiClient;

    @Before
    public void setup() {
        apiClient = mock(N26ApiClient.class);
        transactionalAccountFetcher = new N26TransactionalAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchAndMapCheckingAccount() {
        // given
        AccountsResponse accountsResponse = createAccountsResponse(ACCOUNT_RESPONSE_JSON);
        AccountBalanceResponse accountBalanceResponse =
                createAccountBalanceResponse(ACCOUNT_BALANCE_RESPONSE_JSON);
        when(apiClient.getAccounts()).thenReturn(accountsResponse);
        when(apiClient.getAccountBalance(RESOURCE_ID)).thenReturn(accountBalanceResponse);

        // when
        List<TransactionalAccount> accounts = transactionalAccountFetcher.fetchAccounts();

        // then
        assertThat(accounts).hasSize(1);
        TransactionalAccount transactionalAccount = accounts.get(0);
        assertThat(transactionalAccount.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(transactionalAccount.getApiIdentifier()).isEqualTo(RESOURCE_ID);
        assertThat(transactionalAccount.getAccountNumber()).isEqualTo(ACCOUNT_IBAN);
        assertThat(transactionalAccount.getApiIdentifier()).isEqualTo(RESOURCE_ID);
        IdModule idModule = transactionalAccount.getIdModule();
        assertThat(idModule.getAccountNumber()).isEqualTo(ACCOUNT_IBAN);
        assertThat(idModule.getUniqueId()).isEqualTo(ACCOUNT_IBAN);
        assertThat(idModule.getAccountName()).isEqualTo(ACCOUNT_NAME);
        assertThat(idModule.getIdentifiers()).hasSize(1);
        AccountIdentifier accountIdentifier = idModule.getIdentifiers().stream().findFirst().get();
        assertThat(accountIdentifier.getType()).isEqualTo(Type.IBAN);
        assertThat(accountIdentifier.getIdentifier()).isEqualTo(ACCOUNT_IBAN);
        assertThat(transactionalAccount.getExactBalance().getCurrencyCode()).isEqualTo(CURRENCY);
        assertThat(transactionalAccount.getExactBalance().getExactValue())
                .isEqualTo(new BigDecimal(CURRENT_BALANCE));
        assertThat(transactionalAccount.getExactAvailableBalance().getCurrencyCode())
                .isEqualTo(CURRENCY);
        assertThat(transactionalAccount.getExactAvailableBalance().getExactValue())
                .isEqualTo(new BigDecimal(AVAILABLE_BALANCE));
    }

    @Test
    public void shouldNotMapEmptyAccountResponse() {
        // given
        AccountsResponse accountsResponse = createAccountsResponse(EMPTY_ACCOUNT_RESPONSE_JSON);
        when(apiClient.getAccounts()).thenReturn(accountsResponse);
        when(apiClient.getAccountBalance(RESOURCE_ID)).thenReturn(new AccountBalanceResponse());

        // when
        List<TransactionalAccount> accounts = transactionalAccountFetcher.fetchAccounts();

        // then
        assertThat(accounts).isEmpty();
    }

    @Test
    public void shouldThrowExceptionWhenAvailableBalanceEmpty() {
        // given
        AccountsResponse accountsResponse = createAccountsResponse(ACCOUNT_RESPONSE_JSON);
        AccountBalanceResponse accountBalanceResponse =
                createAccountBalanceResponse(
                        ACCOUNT_BALANCE_RESPONSE_JSON_WITHOUT_AVAILABLE_BALANCE);
        when(apiClient.getAccounts()).thenReturn(accountsResponse);
        when(apiClient.getAccountBalance(RESOURCE_ID)).thenReturn(accountBalanceResponse);

        // then
        assertThatExceptionOfType(CheckingAccountRefreshException.class)
                .isThrownBy(transactionalAccountFetcher::fetchAccounts)
                .withNoCause()
                .withMessage("Available balance not found in response");
    }

    @Test
    public void shouldThrowExceptionWhenCurrentBalanceEmpty() {
        // given
        AccountsResponse accountsResponse = createAccountsResponse(ACCOUNT_RESPONSE_JSON);
        AccountBalanceResponse accountBalanceResponse =
                createAccountBalanceResponse(ACCOUNT_BALANCE_RESPONSE_JSON_WITHOUT_CURRENT_BALANCE);
        when(apiClient.getAccounts()).thenReturn(accountsResponse);
        when(apiClient.getAccountBalance(RESOURCE_ID)).thenReturn(accountBalanceResponse);

        // then
        assertThatExceptionOfType(CheckingAccountRefreshException.class)
                .isThrownBy(transactionalAccountFetcher::fetchAccounts)
                .withNoCause()
                .withMessage("Current balance not found in response");
    }

    @Test
    public void shouldThrowExceptionWhenIbanEmpty() {
        // given
        AccountsResponse accountsResponse =
                createAccountsResponse(ACCOUNT_RESPONSE_JSON_WITHOUT_IBAN);
        AccountBalanceResponse accountBalanceResponse =
                createAccountBalanceResponse(ACCOUNT_BALANCE_RESPONSE_JSON);
        when(apiClient.getAccounts()).thenReturn(accountsResponse);
        when(apiClient.getAccountBalance(RESOURCE_ID)).thenReturn(accountBalanceResponse);

        // then
        assertThatExceptionOfType(CheckingAccountRefreshException.class)
                .isThrownBy(transactionalAccountFetcher::fetchAccounts)
                .withNoCause()
                .withMessage("IBAN not found in response");
    }

    @Test
    public void shouldFetchTransactions() {
        // given
        final TransactionalAccount transactionalAccount = createTransactionalAccount();
        final AccountTransactionsResponse accountTransactionsResponse =
                createAccountTransactionsResponse(TRANSACTIONS_RESPONSE_JSON);
        when(apiClient.getAccountTransactions(RESOURCE_ID, null))
                .thenReturn(accountTransactionsResponse);

        // when
        TransactionKeyPaginatorResponse<String> transactions =
                transactionalAccountFetcher.getTransactionsFor(transactionalAccount, null);

        // then
        assertThat(transactions.nextKey()).isEqualTo(OFFSET_1);
        assertThat(transactions.canFetchMore()).isEqualTo(Optional.of(true));
        assertThat(transactions.getTinkTransactions()).hasSize(2);
    }

    @Test
    public void shouldNotFetchMoreTransactions() {
        // given
        final TransactionalAccount transactionalAccount = createTransactionalAccount();
        final String offset = "OFFSET_KEY";
        final AccountTransactionsResponse accountTransactionsResponse =
                createAccountTransactionsResponse(EMPTY_TRANSACTIONS_RESPONSE_JSON);
        when(apiClient.getAccountTransactions(RESOURCE_ID, offset))
                .thenReturn(accountTransactionsResponse);

        // when
        TransactionKeyPaginatorResponse<String> transactions =
                transactionalAccountFetcher.getTransactionsFor(transactionalAccount, offset);

        // then
        assertThat(transactions.nextKey()).isEqualTo(OFFSET_2);
        assertThat(transactions.canFetchMore()).isEqualTo(Optional.of(false));
        assertThat(transactions.getTinkTransactions()).isEmpty();
    }

    @SneakyThrows
    private AccountsResponse createAccountsResponse(String json) {
        return MAPPER.readValue(json, AccountsResponse.class);
    }

    @SneakyThrows
    private AccountBalanceResponse createAccountBalanceResponse(String json) {
        return MAPPER.readValue(json, AccountBalanceResponse.class);
    }

    @SneakyThrows
    private AccountTransactionsResponse createAccountTransactionsResponse(String json) {
        return MAPPER.readValue(json, AccountTransactionsResponse.class);
    }

    private TransactionalAccount createTransactionalAccount() {
        BalanceModule.builder().withBalance(ExactCurrencyAmount.of(CURRENT_BALANCE, CURRENCY));
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(
                        BalanceModule.builder()
                                .withBalance(ExactCurrencyAmount.of(CURRENT_BALANCE, CURRENCY))
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(ACCOUNT_IBAN)
                                .withAccountNumber(ACCOUNT_IBAN)
                                .withAccountName(ACCOUNT_NAME)
                                .addIdentifier(AccountIdentifier.create(Type.IBAN, ACCOUNT_IBAN))
                                .build())
                .setApiIdentifier(RESOURCE_ID)
                .build()
                .get();
    }
}
