package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.data.AccountsData;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.data.TransactionsData;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BoursoramaTransactionalAccountFetcherTest {

    private BoursoramaTransactionalAccountFetcher accountFetcher;
    private SessionStorage sessionStorage;
    private BoursoramaApiClient apiClient;

    @Before
    public void setUp() throws Exception {
        sessionStorage = Mockito.mock(SessionStorage.class);
        apiClient = Mockito.mock(BoursoramaApiClient.class);
        accountFetcher = new BoursoramaTransactionalAccountFetcher(apiClient, sessionStorage);

        when(sessionStorage.get(eq(BoursoramaConstants.USER_HASH))).thenReturn("USER_HASH_123");
    }

    @Test
    public void accountsAreCorrectlyMapped() {
        // given
        AccountsResponse accountsResponse =
                SerializationUtils.deserializeFromString(
                        AccountsData.FETCH_ACCOUNTS_RESPONSE, AccountsResponse.class);

        BalanceResponse balanceResponse =
                SerializationUtils.deserializeFromString(
                        AccountsData.FETCH_BALANCES_RESPONSE, BalanceResponse.class);

        // when
        when(apiClient.fetchAccounts(eq("USER_HASH_123"))).thenReturn(accountsResponse);
        when(apiClient.fetchBalances(eq("USER_HASH_123"), anyString())).thenReturn(balanceResponse);

        List<TransactionalAccount> accounts = new ArrayList<>(accountFetcher.fetchAccounts());

        // then
        assertThat(accounts.size()).isEqualTo(1).as("Credit and Debit cards are skipped");
        assertAccountEquals(
                accounts.get(0),
                "FR4810096000504122942551L38",
                "Compte factice pour la sandbox DSP2",
                "005001",
                1642.68,
                "EUR",
                AccountTypes.CHECKING,
                "3B9F0FCF487ECF9FCDC4CBAFDD0A2E6D");
    }

    @Test
    public void whenBalanceIsUnavailable_ExceptionIsThrown() {
        // given
        AccountsResponse accountsResponse =
                SerializationUtils.deserializeFromString(
                        AccountsData.FETCH_ACCOUNTS_RESPONSE, AccountsResponse.class);

        BalanceResponse balanceResponse =
                SerializationUtils.deserializeFromString(
                        AccountsData.FETCH_BALANCES_RESPONSE_INVALID_BALANCE_TYPE,
                        BalanceResponse.class);

        // when
        when(apiClient.fetchAccounts(eq("USER_HASH_123"))).thenReturn(accountsResponse);
        when(apiClient.fetchBalances(eq("USER_HASH_123"), anyString())).thenReturn(balanceResponse);

        // then
        Throwable thrown = catchThrowable(accountFetcher::fetchAccounts);
        Assertions.assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3B9F0FCF487ECF9FCDC4CBAFDD0A2E6D")
                .as("exception contains account id, for which balance is unavailable");
    }

    @Test
    public void transactionsAreCorrectlyMapped() throws ParseException {
        // given
        TransactionalAccount account = mock(TransactionalAccount.class);

        Date dateFrom = new SimpleDateFormat("yyyy-MM-dd").parse("2019-01-01");
        Date dateTo = new SimpleDateFormat("yyyy-MM-dd").parse("2019-03-01");

        TransactionsResponse accountsResponse =
                SerializationUtils.deserializeFromString(
                        TransactionsData.FETCH_TRANSACTIONS_RESPONSE, TransactionsResponse.class);

        // when
        when(account.getApiIdentifier()).thenReturn("123456");
        when(apiClient.fetchTransactions("USER_HASH_123", "123456", dateFrom, dateTo))
                .thenReturn(accountsResponse);

        PaginatorResponse paginatorResponse =
                accountFetcher.getTransactionsFor(account, dateFrom, dateTo);

        // then
        assertThat(paginatorResponse.canFetchMore()).isEqualTo(Optional.empty());

        ArrayList<Transaction> transactions =
                new ArrayList<>(paginatorResponse.getTinkTransactions());

        assertThat(transactions.size()).isEqualTo(3).as("All transactionas are mapped");
        assertThat(transactions.get(1).getDate())
                .isEqualToIgnoringHours("2018-06-26")
                .as("booking date is mapped as transaction date");
        assertThat(transactions.get(0).getDescription()).contains("PRLV SEPA SFR   ");

        assertThat(transactions.get(0).getExactAmount().getDoubleValue()).isEqualTo(-10.0);
        assertThat(transactions.get(1).getExactAmount().getDoubleValue()).isEqualTo(+6.0);
        assertThat(transactions.get(0).getExactAmount().getCurrencyCode()).isEqualTo("PLN");
        assertThat(transactions.get(1).getExactAmount().getCurrencyCode()).isEqualTo("EUR");
    }

    private void assertAccountEquals(
            TransactionalAccount account,
            String accountNumber,
            String accountName,
            String productName,
            double balanceValue,
            String balanceCurrency,
            AccountTypes type,
            String apiIdentifier) {

        assertThat(account.getAccountNumber()).isEqualTo(accountNumber);

        assertThat(account.getIdModule().getProductName()).isEqualTo(productName);
        assertThat(account.getIdModule().getAccountNumber()).isEqualTo(accountNumber);
        assertThat(account.getIdModule().getAccountName()).isEqualTo(accountName);
        assertThat(account.getIdModule().getUniqueId()).isEqualTo(apiIdentifier);

        assertThat(account.getExactBalance().getDoubleValue()).isEqualTo(balanceValue);
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo(balanceCurrency);

        assertThat(account.getType()).isEqualTo(type);
        assertThat(account.getApiIdentifier()).isEqualTo(apiIdentifier);
    }
}
