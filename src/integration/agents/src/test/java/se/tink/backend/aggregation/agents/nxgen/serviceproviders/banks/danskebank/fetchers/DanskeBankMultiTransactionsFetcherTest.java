package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Test;
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
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DanskeBankMultiTransactionsFetcherTest {

    private static final String TRANSACTIONS_CONTINUATION_KEY = "xyz";

    private static final String ENTITIES_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/danskebank/resources/";

    private static final String ACCOUNT_TRANSACTIONS_WITHOUT_CONTINUATION_PATH =
            ENTITIES_FILE_PATH + "accountTransactionsWithoutContinuation.json";
    private static final String ACCOUNT_TRANSACTIONS_WITH_CONTINUATION_PATH =
            ENTITIES_FILE_PATH + "accountTransactionsWithContinuation.json";

    private DanskeBankApiClient client;
    private DanskeBankMultiTransactionsFetcher<TransactionalAccount> fetcher;

    private TransactionalAccount transactionalAccount;
    private Date dateTo;
    private Date dateFrom;
    private LocalDate now;

    @Before
    public void before() {
        client = mock(DanskeBankApiClient.class);
        fetcher = new DanskeBankMultiTransactionsFetcher<>(client, "en");

        transactionalAccount = getTransactionalAccount();
        now = LocalDate.of(2021, 3, 30);
        dateTo = parseLocalDate(now);
        dateFrom = parseLocalDate(now.minusDays(89));
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
                fetcher.getTransactionsFor(transactionalAccount, dateFrom, dateTo);
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
    public void shouldFetchTransactionsUsing4PartitionDateRanges() {
        // given
        mockAllTransactionsResponse(ACCOUNT_TRANSACTIONS_WITHOUT_CONTINUATION_PATH);

        // when
        fetcher.getTransactionsFor(transactionalAccount, dateFrom, dateTo);

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
                                .withUniqueIdentifier("UNIQUE_IDENTIFIER")
                                .withAccountNumber("4899999999")
                                .withAccountName("")
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifierType.IBAN, ""))
                                .build())
                .setApiIdentifier("4899999999")
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
