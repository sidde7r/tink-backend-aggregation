package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants.Transactions.ZONE_ID;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DanskeBankMultiTransactionsFetcherTest {

    private static final String TRANSACTIONS_CONTINUATION_KEY = "xyz";
    private static final String ACCOUNT_NUMBER = "4899999999";

    private static final String ENTITIES_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/danskebank/resources/";

    private static final String ACCOUNT_TRANSACTIONS_WITHOUT_CONTINUATION_PATH =
            ENTITIES_FILE_PATH + "accountTransactionsWithoutContinuation.json";
    private static final String ACCOUNT_TRANSACTIONS_WITH_CONTINUATION_PATH =
            ENTITIES_FILE_PATH + "accountTransactionsWithContinuation.json";

    private DanskeBankApiClient client;
    private DanskeBankMultiTransactionsFetcher<TransactionalAccount> fetcherForInitialRequest;
    private DanskeBankMultiTransactionsFetcher<TransactionalAccount> fetcherForNotInitialRefresh;

    private TransactionalAccount transactionalAccount;
    private Date dateTo;
    private Date dateFrom;
    private LocalDate now;

    @Before
    public void before() {
        client = mock(DanskeBankApiClient.class);
        transactionalAccount = getTransactionalAccount();
        now = LocalDate.of(2021, 3, 30);

        dateTo = parseLocalDate(now);
        dateFrom = parseLocalDate(now.minusDays(89));

        mockSituationWhenThereIsNoAccountInContext();
        mockSituationWhenThereIsAccountWithCertainDateInContext();
    }

    private void mockSituationWhenThereIsNoAccountInContext() {
        CredentialsRequest credentialsRequest = Mockito.mock(CredentialsRequest.class);
        List<Account> credentialsRequestAccounts = new LinkedList<>();
        Mockito.when(credentialsRequest.getAccounts()).thenReturn(credentialsRequestAccounts);
        fetcherForInitialRequest =
                new DanskeBankMultiTransactionsFetcher<>(client, "en", credentialsRequest);
    }

    private void mockSituationWhenThereIsAccountWithCertainDateInContext() {
        CredentialsRequest credentialsRequest = Mockito.mock(CredentialsRequest.class);
        List<Account> credentialsRequestAccounts = new LinkedList<>();
        Account account = new Account();
        account.setCertainDate(Date.from(now.minusDays(10).atStartOfDay(ZONE_ID).toInstant()));
        account.setBankId(ACCOUNT_NUMBER);
        credentialsRequestAccounts.add(account);
        Mockito.when(credentialsRequest.getAccounts()).thenReturn(credentialsRequestAccounts);
        fetcherForNotInitialRefresh =
                new DanskeBankMultiTransactionsFetcher<>(client, "en", credentialsRequest);
    }

    @Test
    public void shouldFetchTransactionsForAccountAndMapCorrectly() {
        // given
        mockAccountTransactionsQueriedByDate(
                "", ACCOUNT_TRANSACTIONS_WITH_CONTINUATION_PATH, now.minusDays(89), now);
        mockAccountTransactionsQueriedByDate(
                "",
                ACCOUNT_TRANSACTIONS_WITHOUT_CONTINUATION_PATH,
                now.minusDays(179),
                now.minusDays(90));
        mockAccountTransactionsQueriedByDate(
                "",
                ACCOUNT_TRANSACTIONS_WITHOUT_CONTINUATION_PATH,
                now.minusDays(269),
                now.minusDays(180));
        mockAccountTransactionsQueriedByDate(
                "",
                ACCOUNT_TRANSACTIONS_WITH_CONTINUATION_PATH,
                now.minusDays(359),
                now.minusDays(270));
        mockAccountTransactionsQueriedByDate(
                TRANSACTIONS_CONTINUATION_KEY,
                ACCOUNT_TRANSACTIONS_WITHOUT_CONTINUATION_PATH,
                now.minusDays(89),
                now);
        mockAccountTransactionsQueriedByDate(
                TRANSACTIONS_CONTINUATION_KEY,
                ACCOUNT_TRANSACTIONS_WITHOUT_CONTINUATION_PATH,
                now.minusDays(359),
                now.minusDays(270));

        // when
        PaginatorResponse paginatorResponse =
                fetcherForInitialRequest.getTransactionsFor(transactionalAccount, dateFrom, dateTo);
        // then
        assertThat(paginatorResponse.getTinkTransactions()).hasSize(12);
        // and
        Transaction transaction1 =
                paginatorResponse.getTinkTransactions().stream()
                        .filter(
                                transaction ->
                                        "Til Allan konto".equals(transaction.getDescription()))
                        .findAny()
                        .orElseThrow(() -> new IllegalStateException("Transaction not found"));
        assertThat(transaction1.getExactAmount().getCurrencyCode()).isEqualTo("DKK");
        assertThat(transaction1.getExactAmount().getExactValue())
                .isEqualByComparingTo(new BigDecimal("-200.00"));
        assertThat(transaction1.getDate()).isEqualToIgnoringHours("2020-01-13");
    }

    @Test
    public void shouldFetchTransactionsForAccountAndMapCorrectlyForNotInitialRefresh() {
        // given
        mockAccountTransactionsQueriedByDate(
                "", ACCOUNT_TRANSACTIONS_WITHOUT_CONTINUATION_PATH, now.minusDays(89), now);

        // when
        PaginatorResponse paginatorResponse =
                fetcherForNotInitialRefresh.getTransactionsFor(
                        transactionalAccount, dateFrom, dateTo);
        // then
        assertThat(paginatorResponse.getTinkTransactions()).hasSize(2);
        // and
        Transaction transaction1 =
                paginatorResponse.getTinkTransactions().stream()
                        .filter(
                                transaction ->
                                        "Til Allan konto".equals(transaction.getDescription()))
                        .findAny()
                        .orElseThrow(() -> new IllegalStateException("Transaction not found"));
        assertThat(transaction1.getExactAmount().getCurrencyCode()).isEqualTo("DKK");
        assertThat(transaction1.getExactAmount().getExactValue())
                .isEqualByComparingTo(new BigDecimal("-200.00"));
        assertThat(transaction1.getDate()).isEqualToIgnoringHours("2020-01-13");
    }

    @Test
    public void shouldFetchTransactionsUsing4PartitionDateRanges() {
        // given
        mockAllTransactionsResponse(ACCOUNT_TRANSACTIONS_WITHOUT_CONTINUATION_PATH);

        // when
        fetcherForInitialRequest.getTransactionsFor(transactionalAccount, dateFrom, dateTo);

        // then
        IntStream.range(0, 4)
                .forEach(
                        partition -> {
                            ListTransactionsRequest transactionsRequest =
                                    prepareTransactionsRequest(transactionalAccount, partition);
                            verify(client).listTransactions(eq(transactionsRequest));
                        });
    }

    private ListTransactionsRequest prepareTransactionsRequest(
            TransactionalAccount account, Integer partition) {
        return ListTransactionsRequest.create(
                "en",
                account.getApiIdentifier(),
                formatLocalDateToString(now.minusDays((partition + 1) * 90 - 1)),
                formatLocalDateToString(now.minusDays(partition * 90)));
    }

    private void mockAllTransactionsResponse(String path) {
        IntStream.range(0, 4)
                .forEach(
                        partition ->
                                mockAccountTransactionsQueriedByDate(
                                        "",
                                        path,
                                        now.minusDays((partition + 1) * 90 - 1),
                                        now.minusDays(partition * 90)));
    }

    private void mockAccountTransactionsQueriedByDate(
            String continuationKey,
            String path,
            LocalDate localDateFrom,
            LocalDate localDateToString) {
        ListTransactionsRequest transactionsRequest =
                ListTransactionsRequest.create(
                        "en",
                        transactionalAccount.getApiIdentifier(),
                        formatLocalDateToString(localDateFrom),
                        formatLocalDateToString(localDateToString));

        transactionsRequest.setRepositionKey(continuationKey);
        when(client.listTransactions(eq(transactionsRequest)))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(path), ListTransactionsResponse.class));
    }

    private TransactionalAccount getTransactionalAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(ExactCurrencyAmount.inDKK(1)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(ACCOUNT_NUMBER)
                                .withAccountNumber(ACCOUNT_NUMBER)
                                .withAccountName("")
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifierType.IBAN, ""))
                                .build())
                .setApiIdentifier(ACCOUNT_NUMBER)
                .build()
                .orElseThrow(IllegalStateException::new);
    }

    private String formatLocalDateToString(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private Date parseLocalDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
