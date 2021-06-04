package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.transactionalaccount.RuralviaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class RuralviaTransactionalAccountFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/ruralvia/resources";

    private static RuralviaApiClient apiClient;
    private static String htmlGlobalPosition;
    private static RuralviaTransactionalAccountFetcher accountFetcher;
    private static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

    @BeforeClass
    public static void setUp() throws IOException {
        htmlGlobalPosition =
                new String(Files.readAllBytes(Paths.get(TEST_DATA_PATH, "globalPosition.html")));
        apiClient = mock(RuralviaApiClient.class);
        accountFetcher = new RuralviaTransactionalAccountFetcher(apiClient);
    }

    @Test
    public void fetchAccountShouldFetch() {
        // given
        given(apiClient.getGlobalPositionHtml()).willReturn(htmlGlobalPosition);

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
    public void getTransactionsForShouldFetch() throws IOException, ParseException {
        // given

        given(apiClient.getGlobalPositionHtml()).willReturn(htmlGlobalPosition);
        given(apiClient.navigateAccountTransactionFirstRequest(any(), any()))
                .willReturn(
                        new String(
                                Files.readAllBytes(
                                        Paths.get(
                                                TEST_DATA_PATH, "accountsLastTransactions.html"))));
        given(apiClient.navigateAccountTransactionsBetweenDates(any(), any()))
                .willReturn(
                        new String(
                                Files.readAllBytes(
                                        Paths.get(
                                                TEST_DATA_PATH,
                                                "accountsTransactionsForLast3Months.html"))));
        given(apiClient.createBodyFormRequest(any(), any())).willReturn(mock(RequestBuilder.class));

        accountFetcher.fetchAccounts();

        // when
        TransactionKeyPaginatorResponse<String> paginatorResponse =
                accountFetcher.getTransactionsFor(
                        mockedAccount().toTinkAccount().get(),
                        formatter.parse("29-04-2021"),
                        formatter.parse("28-01-2021"));

        // then
        assertEquals(50, paginatorResponse.getTinkTransactions().size());
    }

    @Test
    public void getTransactionsForShouldReturnEmptyWhenNotFoundAccount() throws ParseException {
        // given
        given(apiClient.getGlobalPositionHtml()).willReturn("");
        accountFetcher.fetchAccounts();

        // when
        TransactionKeyPaginatorResponse<String> paginatorResponse =
                accountFetcher.getTransactionsFor(
                        mockedAccount().toTinkAccount().get(),
                        formatter.parse("29-04-2021"),
                        formatter.parse("28-01-2021"));

        // then
        assertTrue(paginatorResponse.getTinkTransactions().size() == 0);
    }

    private AccountEntity mockedAccount() {
        return AccountEntity.builder()
                .accountNumber("ES5000818447506159992545")
                .accountAlias("C/C PARTICULARES")
                .currency("EUR")
                .balance("100,00")
                .build();
    }
}
