package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.fetcher;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.fetcher.AggregationTransactionAsserts.assertThat;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.fetcher.ResponseTestData.*;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.fetcher.TransactionalAccountAsserts.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.HandelsbankenAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.HandelsbankenFiApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.HandelsbankenBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionalAccountFetcherTest {
    private static final Date DUMMY_PAST_DATE = new Date(2020, 02, 11);
    private static final Date DUMMY_FUTURE_DATE = new Date(2021, 02, 11);
    private static final String DUMMY_ACCOUNT_ID = "ee02d6d8-6225-467d-bc69-a0dc03894642";
    private static final LocalDate MAX_PERIOD_TRANSACTIONS = LocalDate.now().minusDays(90);
    private HandelsbankenBaseTransactionalAccountFetcher objectUnderTest;
    private HandelsbankenFiApiClient apiClient;

    @Before
    public void setUp() {
        apiClient = mock(HandelsbankenFiApiClient.class);
        objectUnderTest =
                new HandelsbankenBaseTransactionalAccountFetcher(
                        apiClient, MAX_PERIOD_TRANSACTIONS);
        objectUnderTest.setConverter(new HandelsbankenAccountConverter());
    }

    @Test
    public void shouldMapAccountsResponseIntoTransactionalAccount() {
        // given
        given(apiClient.getAccountList()).willReturn(ACCOUNT_RESPONSE);
        given(apiClient.getAccountDetails(DUMMY_ACCOUNT_ID)).willReturn(BALANCE_ACCOUNT_RESPONSE);

        // when
        Collection<TransactionalAccount> transactionalAccounts = objectUnderTest.fetchAccounts();

        // then
        TransactionalAccount account =
                transactionalAccounts.stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("No accounts found"));

        assertThat(account).isCheckingAccount();
        assertThat(account).hasAccountNumber("FI1234123412341234");
        assertThat(account).hasBalance(new ExactCurrencyAmount(BigDecimal.valueOf(6964.34), "EUR"));
        assertThat(account).hasHolder("DUMMY USER");
    }

    @Test
    public void shouldNotMapAccountsWithoutBalance() {
        // given
        given(apiClient.getAccountList()).willReturn(ACCOUNT_RESPONSE);
        given(apiClient.getAccountDetails(DUMMY_ACCOUNT_ID))
                .willReturn(EMPTY_BALANCE_ACCOUNT_RESPONSE);

        // when
        Collection<TransactionalAccount> transactionalAccounts = objectUnderTest.fetchAccounts();

        // then
        Assertions.assertThat(transactionalAccounts).isEmpty();
    }

    @Test
    public void shouldNegateTransactionsAmountWhenDebited() {
        // given
        TransactionalAccount account = mock(TransactionalAccount.class);
        given(apiClient.getTransactions(DUMMY_ACCOUNT_ID, DUMMY_PAST_DATE, DUMMY_FUTURE_DATE))
                .willReturn(TRANSACTION_DEBITED_RESPONSE);
        given(account.getApiIdentifier()).willReturn(DUMMY_ACCOUNT_ID);

        // when
        PaginatorResponse response =
                objectUnderTest.getTransactionsFor(account, DUMMY_PAST_DATE, DUMMY_FUTURE_DATE);
        Collection<? extends Transaction> transactions = response.getTinkTransactions();
        AggregationTransaction transaction =
                transactions.stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("No transaction found"));

        // then
        assertThat(transaction)
                .hasExactAmount(new ExactCurrencyAmount(BigDecimal.valueOf(-3), "EUR"));
        assertThat(transaction).hasDescription("DUMMY NAME 1.-31.12.2020");
    }

    @Test
    public void shouldNotNegateTransactionsAmountWhenCredited() {
        // given
        TransactionalAccount account = mock(TransactionalAccount.class);
        given(apiClient.getTransactions(DUMMY_ACCOUNT_ID, DUMMY_PAST_DATE, DUMMY_FUTURE_DATE))
                .willReturn(TRANSACTION_CREDITED_RESPONSE);
        given(account.getApiIdentifier()).willReturn(DUMMY_ACCOUNT_ID);

        // when
        PaginatorResponse response =
                objectUnderTest.getTransactionsFor(account, DUMMY_PAST_DATE, DUMMY_FUTURE_DATE);
        Collection<? extends Transaction> transactions = response.getTinkTransactions();
        AggregationTransaction transaction =
                transactions.stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("No transaction found"));

        // then
        assertThat(transaction)
                .hasExactAmount(new ExactCurrencyAmount(BigDecimal.valueOf(3), "EUR"));
        assertThat(transaction).hasDescription("DUMMY NAME 1.-31.12.2020");
    }

    @Test
    public void shouldNotFetchTransactionsIfFromDateIsLaterThanToDate() {
        // given
        TransactionalAccount account = mock(TransactionalAccount.class);
        given(apiClient.getTransactions(DUMMY_ACCOUNT_ID, DUMMY_PAST_DATE, DUMMY_FUTURE_DATE))
                .willReturn(TRANSACTION_DEBITED_RESPONSE);
        given(account.getApiIdentifier()).willReturn(DUMMY_ACCOUNT_ID);

        // when
        PaginatorResponse response =
                objectUnderTest.getTransactionsFor(account, DUMMY_FUTURE_DATE, DUMMY_PAST_DATE);
        Collection<? extends Transaction> transactions = response.getTinkTransactions();

        // then
        Assertions.assertThat(transactions).isEmpty();
    }
}
