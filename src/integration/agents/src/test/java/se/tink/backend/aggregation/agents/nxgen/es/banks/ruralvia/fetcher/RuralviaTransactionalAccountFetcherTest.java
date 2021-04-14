package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Locale;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.transactionalaccount.RuralviaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class RuralviaTransactionalAccountFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/ruralvia/resources";

    private static RuralviaApiClient apiClient;
    private static String htmlGlobalPosition;

    @BeforeClass
    public static void setUp() throws IOException {
        htmlGlobalPosition =
                new String(Files.readAllBytes(Paths.get(TEST_DATA_PATH, "globalPosition.html")));
        apiClient = mock(RuralviaApiClient.class);
    }

    @Test
    public void fetchAccountShouldFetch() {
        // given
        RuralviaTransactionalAccountFetcher accountFetcher =
                new RuralviaTransactionalAccountFetcher(apiClient);
        // when
        Collection<TransactionalAccount> fetchedAccounts = accountFetcher.fetchAccounts();

        // then
        TransactionalAccount mockedAccount = mockedAccount().toTinkAccount().get();
        TransactionalAccount fetchedAccount = fetchedAccounts.stream().findFirst().get();
        assertEquals(mockedAccount.getAccountNumber(), fetchedAccount.getAccountNumber());
        assertEquals(mockedAccount.getIdentifiers(), fetchedAccount.getIdentifiers());
        assertTrue(
                mockedAccount
                        .getExactBalance()
                        .getExactValue()
                        .equals(fetchedAccount.getExactBalance().getExactValue()));
    }

    @Test
    public void getTransactionsForShouldFetch() throws IOException {
        // given
        when(apiClient.getGlobalPositionHtml()).thenReturn(htmlGlobalPosition);
        RuralviaTransactionalAccountFetcher accountFetcher =
                new RuralviaTransactionalAccountFetcher(apiClient);
        when(apiClient.navigateAccountTransactionFirstRequest(Mockito.any()))
                .thenReturn(
                        new String(
                                Files.readAllBytes(
                                        Paths.get(TEST_DATA_PATH, "lastTransactions.html"))));
        when(apiClient.navigateAccountTransactionsBetweenDates(Mockito.any()))
                .thenReturn(
                        new String(
                                Files.readAllBytes(
                                        Paths.get(
                                                TEST_DATA_PATH,
                                                "transactionsForLast3Months.html"))));
        when(apiClient.createBodyFormRequest(Mockito.any(), Mockito.any()))
                .thenReturn(mock(RequestBuilder.class));

        // when
        TransactionKeyPaginatorResponse<String> paginatorResponse =
                accountFetcher.getTransactionsFor(mockedAccount().toTinkAccount().get(), "");

        // then
        assertEquals(50, paginatorResponse.getTinkTransactions().size());
    }

    @Test
    public void getTransactionsForShouldReturnEmptyWhenNotFoundAccount() {
        // given
        when(apiClient.getGlobalPositionHtml()).thenReturn("");
        RuralviaTransactionalAccountFetcher accountFetcher =
                new RuralviaTransactionalAccountFetcher(apiClient);

        // when
        TransactionKeyPaginatorResponse<String> paginatorResponse =
                accountFetcher.getTransactionsFor(mockedAccount().toTinkAccount().get(), "");

        // then
        assertTrue(paginatorResponse.getTinkTransactions().size() == 0);
    }

    @Test
    public void parseAmountShouldParseAsExpected() {
        // given
        RuralviaTransactionalAccountFetcher accountFetcher =
                new RuralviaTransactionalAccountFetcher(apiClient);
        Locale locale = new Locale("es", "ES");
        String amountToClean = "\t\t 100, 00 \n";
        String currency = "EUR";

        String amount2 = "1234567890000,1234567";

        // when
        ExactCurrencyAmount result = accountFetcher.parseAmount(amountToClean, currency);
        ExactCurrencyAmount result2 = accountFetcher.parseAmount(amount2, currency);

        // then
        assertTrue(result.getCurrencyCode().equals(currency));
        assertTrue(result.getStringValue(locale).equals("100,00"));

        assertTrue(result2.getStringValue(locale).equals("1.234.567.890.000,12"));
    }

    private AccountEntity mockedAccount() {
        AccountEntity accountEntity = new AccountEntity();

        accountEntity.setAccountNumber("ES5000818447506159992545");
        accountEntity.setAccountAlias("C/C PARTICULARES");
        accountEntity.setCurrency("EUR");
        accountEntity.setBalance("100,00");

        return accountEntity;
    }
}
