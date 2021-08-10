package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaConstants.ZONE_ID;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.data.AccountsData;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.data.TransactionsData;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BoursoramaTransactionalAccountFetcherTest {
    private static final LocalDateTime NOW = LocalDateTime.parse("2021-06-08T10:11:30");

    private BoursoramaAccountTransactionalAccountFetcher accountFetcher;
    private BoursoramaApiClient apiClient;

    @Before
    public void setUp() throws Exception {
        LocalDateTimeSource localDateTimeSource = mock(LocalDateTimeSource.class);
        Mockito.when(localDateTimeSource.now()).thenReturn(NOW);

        apiClient = Mockito.mock(BoursoramaApiClient.class);
        accountFetcher =
                new BoursoramaAccountTransactionalAccountFetcher(
                        apiClient, localDateTimeSource, new BoursoramaHolderNamesExtractor());
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
        when(apiClient.fetchAccounts()).thenReturn(accountsResponse);
        when(apiClient.fetchBalances(anyString())).thenReturn(balanceResponse);

        List<TransactionalAccount> accounts = new ArrayList<>(accountFetcher.fetchAccounts());

        // then
        assertThat(accounts.size()).isEqualTo(1);
        TransactionalAccount account = accounts.get(0);

        assertThat(account.getAccountNumber()).isEqualTo("FR4810096000504122942551L38");

        assertThat(account.getIdModule().getProductName()).isEqualTo("CAV - BOURSORAMA BANQUE");
        assertThat(account.getIdModule().getAccountNumber())
                .isEqualTo("FR4810096000504122942551L38");
        assertThat(account.getIdModule().getAccountName()).isEqualTo("CAV - BOURSORAMA BANQUE");
        assertThat(account.getIdModule().getUniqueId())
                .isEqualTo("3B9F0FCF487ECF9FCDC4CBAFDD0A2E6D");

        assertThat(account.getExactBalance().getDoubleValue()).isEqualTo(1642.68);
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");

        assertThat(account.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(account.getApiIdentifier()).isEqualTo("3B9F0FCF487ECF9FCDC4CBAFDD0A2E6D");
        assertThat(account.getHolderName().toString()).isEqualTo("Name Surname");
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
        when(apiClient.fetchAccounts()).thenReturn(accountsResponse);
        when(apiClient.fetchBalances(anyString())).thenReturn(balanceResponse);

        // then
        Throwable thrown = catchThrowable(accountFetcher::fetchAccounts);
        Assertions.assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void transactionsAreCorrectlyMapped() {
        // given
        TransactionalAccount account = mock(TransactionalAccount.class);

        LocalDate localDateTo = NOW.toLocalDate();
        LocalDate localDateFrom = localDateTo.minusDays(89);
        Date dateFrom = convertLocalDateToDate(localDateFrom);
        Date dateTo = convertLocalDateToDate(localDateTo);

        TransactionsResponse accountsResponse =
                SerializationUtils.deserializeFromString(
                        TransactionsData.FETCH_TRANSACTIONS_RESPONSE, TransactionsResponse.class);

        when(account.getApiIdentifier()).thenReturn("123456");
        when(apiClient.fetchTransactions(any(), any(), any())).thenReturn(accountsResponse);

        // when
        PaginatorResponse paginatorResponse =
                accountFetcher.getTransactionsFor(account, dateFrom, dateTo);

        // then
        assertThat(paginatorResponse.canFetchMore()).isEqualTo(Optional.empty());

        ArrayList<Transaction> transactions =
                new ArrayList<>(paginatorResponse.getTinkTransactions());

        assertThat(transactions.size()).isEqualTo(3);
        assertThat(transactions.get(1).getDate()).isEqualToIgnoringHours("2018-06-26");
        assertThat(transactions.get(0).getDescription()).contains("PRLV SEPA SFR   ");

        assertThat(transactions.get(0).getExactAmount().getDoubleValue()).isEqualTo(-10.0);
        assertThat(transactions.get(1).getExactAmount().getDoubleValue()).isEqualTo(+6.0);
        assertThat(transactions.get(0).getExactAmount().getCurrencyCode()).isEqualTo("PLN");
        assertThat(transactions.get(1).getExactAmount().getCurrencyCode()).isEqualTo("EUR");
    }

    private static Date convertLocalDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZONE_ID).toInstant());
    }
}
