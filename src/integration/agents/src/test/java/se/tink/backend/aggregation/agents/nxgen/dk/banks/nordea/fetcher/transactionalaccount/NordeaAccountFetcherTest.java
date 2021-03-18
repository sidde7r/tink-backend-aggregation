package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaTestData.TransactionalAccountTestData.ACCOUNT_1_API_ID;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaTestData.TransactionalAccountTestData.ACCOUNT_WITH_CREDIT_API_ID;

import io.vavr.collection.Stream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClientMockWrapper;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaTestData.TransactionalAccountTestData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class NordeaAccountFetcherTest {

    private NordeaDkApiClient client;
    private NordeaDkApiClientMockWrapper clientMockWrapper;
    private NordeaAccountFetcher fetcher;

    private TransactionalAccount transactionalAccount;
    private Date dateTo;
    private Date dateFrom;
    private LocalDate now;

    @Before
    public void before() {
        client = mock(NordeaDkApiClient.class);
        clientMockWrapper = new NordeaDkApiClientMockWrapper(client);
        fetcher = new NordeaAccountFetcher(client);

        transactionalAccount = getTransactionalAccount();
        now = LocalDate.now();
        dateTo = parseLocalDate(now);
        dateFrom = parseLocalDate(now.minusMonths(12));
    }

    @Test
    public void shouldFetchAccountsAndMapCorrectly() {
        // given
        clientMockWrapper.mockGetAccountsUsingFile(
                TransactionalAccountTestData.TRANSACTIONAL_ACCOUNTS_FILE);

        // when
        Collection<TransactionalAccount> fetchedAccounts = fetcher.fetchAccounts();

        // then
        assertThat(fetchedAccounts).hasSize(4);
        // and
        TransactionalAccount regularAccount =
                fetchedAccounts.stream()
                        .filter(account -> ACCOUNT_1_API_ID.equals(account.getApiIdentifier()))
                        .findAny()
                        .orElse(null);
        assertRegularAccountValid(regularAccount);

        TransactionalAccount creditAccount =
                fetchedAccounts.stream()
                        .filter(
                                account ->
                                        ACCOUNT_WITH_CREDIT_API_ID.equals(
                                                account.getApiIdentifier()))
                        .findAny()
                        .orElse(null);
        assertCreditAccountValid(creditAccount);
    }

    private void assertRegularAccountValid(TransactionalAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("DKK");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("91844.66"));
        assertThat(account.getExactAvailableBalance().getCurrencyCode()).isEqualTo("DKK");
        assertThat(account.getExactAvailableBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("91844.66"));
        assertThat(account.getExactCreditLimit().getCurrencyCode()).isEqualTo("DKK");
        assertThat(account.getExactCreditLimit().getExactValue())
                .isEqualByComparingTo(new BigDecimal("0.00"));
        assertThat(account.getExactAvailableCredit().getCurrencyCode()).isEqualTo("DKK");
        assertThat(account.getExactAvailableCredit().getExactValue())
                .isEqualByComparingTo(new BigDecimal("0.00"));
        assertThat(account.getAccountNumber()).isEqualTo("DK4520007418529630");
        assertThat(account.getIdModule().getUniqueId()).isEqualTo("7418529630");
    }

    private void assertCreditAccountValid(TransactionalAccount creditAccount) {
        assertThat(creditAccount).isNotNull();
        assertThat(creditAccount.getExactBalance().getCurrencyCode()).isEqualTo("DKK");
        assertThat(creditAccount.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("-10349.23"));
        assertThat(creditAccount.getExactAvailableCredit().getCurrencyCode()).isEqualTo("DKK");
        assertThat(creditAccount.getExactAvailableCredit().getExactValue())
                .isEqualByComparingTo(new BigDecimal("150.77"));
        assertThat(creditAccount.getExactCreditLimit().getCurrencyCode()).isEqualTo("DKK");
        assertThat(creditAccount.getExactCreditLimit().getExactValue())
                .isEqualByComparingTo(new BigDecimal("10500.00"));
        assertThat(creditAccount.getExactAvailableBalance().getCurrencyCode()).isEqualTo("DKK");
        assertThat(creditAccount.getExactAvailableBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("0.00"));
        assertThat(creditAccount.getAccountNumber()).isEqualTo("DK9320002551697498");
        assertThat(creditAccount.getIdModule().getUniqueId()).isEqualTo("2551697498");
    }

    @Test
    public void shouldFetchTransactionsForAccountAndMapCorrectly() {
        // given
        mockAccountTransactionsQueriedByDate(
                null,
                TransactionalAccountTestData.ACCOUNT_TRANSACTIONS_WITH_CONTINUATION_FILE,
                now.minusMonths(3),
                now);
        mockAccountTransactionsQueriedByDate(
                null,
                TransactionalAccountTestData.ACCOUNT_TRANSACTIONS_W_O_CONTINUATION_FILE,
                now.minusMonths(6),
                now.minusMonths(3));
        mockAccountTransactionsQueriedByDate(
                null,
                TransactionalAccountTestData.ACCOUNT_TRANSACTIONS_W_O_CONTINUATION_FILE,
                now.minusMonths(9),
                now.minusMonths(6));
        mockAccountTransactionsQueriedByDate(
                null,
                TransactionalAccountTestData.ACCOUNT_TRANSACTIONS_WITH_CONTINUATION_FILE,
                now.minusMonths(12),
                now.minusMonths(9));
        mockAccountTransactionsQueriedByDate(
                TransactionalAccountTestData.TRANSACTIONS_CONTINUATION_KEY,
                TransactionalAccountTestData.ACCOUNT_TRANSACTIONS_W_O_CONTINUATION_FILE,
                now.minusMonths(3),
                now);
        mockAccountTransactionsQueriedByDate(
                TransactionalAccountTestData.TRANSACTIONS_CONTINUATION_KEY,
                TransactionalAccountTestData.ACCOUNT_TRANSACTIONS_W_O_CONTINUATION_FILE,
                now.minusMonths(12),
                now.minusMonths(9));

        // when
        PaginatorResponse paginatorResponse =
                fetcher.getTransactionsFor(transactionalAccount, dateFrom, dateTo);
        // then
        assertThat(paginatorResponse.getTinkTransactions()).hasSize(14);
        // and
        Transaction transaction1 =
                paginatorResponse.getTinkTransactions().stream()
                        .filter(
                                transaction ->
                                        "Bgs From Nordea Pay".equals(transaction.getDescription()))
                        .findAny()
                        .orElseThrow(() -> new IllegalStateException("Transaction not found"));
        assertThat(transaction1.getExactAmount().getCurrencyCode()).isEqualTo("DKK");
        assertThat(transaction1.getExactAmount().getExactValue())
                .isEqualByComparingTo(new BigDecimal("6100.00"));
        assertThat(transaction1.getDate()).isEqualToIgnoringHours("2020-03-05");
    }

    @Test
    public void shouldFetchTransactionsWithoutDate() {
        // given
        mockAllTransactionsResponse(
                TransactionalAccountTestData.ACCOUNT_TRANSACTIONS_WITHOUT_DATE_FILE);

        // when
        PaginatorResponse paginatorResponse =
                fetcher.getTransactionsFor(transactionalAccount, dateFrom, dateTo);
        // then
        assertThat(paginatorResponse.getTinkTransactions()).hasSize(12);
    }

    @Test
    public void shouldFetchTransactionsUsing4PartitionDateRanges() {
        // given
        mockAllTransactionsResponse(
                TransactionalAccountTestData.ACCOUNT_TRANSACTIONS_W_O_CONTINUATION_FILE);

        // when
        PaginatorResponse paginatorResponse =
                fetcher.getTransactionsFor(transactionalAccount, dateFrom, dateTo);

        // then
        Stream.range(0, 4)
                .forEach(
                        partition ->
                                verify(client)
                                        .getAccountTransactions(
                                                anyString(),
                                                anyString(),
                                                isNull(),
                                                eq(
                                                        formatLocalDatetoString(
                                                                now.minusMonths(
                                                                        (partition + 1) * 3))),
                                                eq(
                                                        formatLocalDatetoString(
                                                                now.minusMonths(partition * 3)))));
    }

    private void mockAllTransactionsResponse(String path) {
        Stream.range(0, 4)
                .forEach(
                        partition ->
                                mockAccountTransactionsQueriedByDate(
                                        null,
                                        path,
                                        now.minusMonths((partition + 1) * 3),
                                        now.minusMonths(partition * 3)));
    }

    private void mockAccountTransactionsQueriedByDate(
            String continuationKey,
            String path,
            LocalDate localDateFrom,
            LocalDate localDateToString) {
        when(client.getAccountTransactions(
                        eq(transactionalAccount.getApiIdentifier()),
                        any(),
                        eq(continuationKey),
                        eq(formatLocalDatetoString(localDateFrom)),
                        eq(formatLocalDatetoString(localDateToString))))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(path), TransactionsResponse.class));
    }

    private TransactionalAccount getTransactionalAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(ExactCurrencyAmount.inDKK(1)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier("UNIQUE_IDENTIFIER")
                                .withAccountNumber("")
                                .withAccountName("")
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifierType.IBAN, ""))
                                .build())
                .putInTemporaryStorage(NordeaDkConstants.StorageKeys.PRODUCT_CODE, "PRODUCT_CODE")
                .setApiIdentifier("API_IDENTIFIER")
                .build()
                .orElseThrow(IllegalStateException::new);
    }

    private String formatLocalDatetoString(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private Date parseLocalDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
