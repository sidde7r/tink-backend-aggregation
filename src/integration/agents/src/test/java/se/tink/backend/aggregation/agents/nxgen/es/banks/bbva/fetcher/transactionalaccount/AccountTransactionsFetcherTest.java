package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount;

import static org.assertj.core.api.Java6Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaTypeMappers.ACCOUNT_TYPE_MAPPER;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.jackson.datatype.VavrModule;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.assertj.core.util.Strings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ParticipantsDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ProductEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.FinancialDashboardResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class AccountTransactionsFetcherTest {

    static final String DATA_PATH = "data/test/agents/es/bbva/";
    BbvaApiClient apiClient;
    Optional<TransactionalAccount> transactionalAccount;
    AccountEntity accountEntity;

    @Before
    public void setup() {
        apiClient = mock(BbvaApiClient.class);
        accountEntity = mock(AccountEntity.class);

        transactionalAccount =
                TransactionalAccount.nxBuilder()
                        .withTypeAndFlagsFrom(ACCOUNT_TYPE_MAPPER, "0CA0000079")
                        .withBalance(BalanceModule.of(ExactCurrencyAmount.of(1, "EUR")))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(
                                                "ES0201821048600000000000"
                                                        .toUpperCase(Locale.ENGLISH))
                                        .withAccountNumber("ES02 0182 1048 6000 0000 0000")
                                        .withAccountName("Cuentas Personales")
                                        .addIdentifier(
                                                AccountIdentifier.create(
                                                        Type.IBAN, "ES0201821048600000000000"))
                                        .build())
                        .setApiIdentifier("ES0XXXXXXXXXXXXXXXXXXXXXXXXXXX")
                        .addParties(Arrays.asList(new Party("OWNER", Role.HOLDER)))
                        .build();
    }

    @Test
    public void testAccountFetcher() throws IOException {
        final FinancialDashboardResponse financialDashboardResponse =
                loadSampleData("financial_dashboard.json", FinancialDashboardResponse.class);
        final ParticipantsDataEntity participantsDataEntity =
                loadSampleData("participants.json", ParticipantsDataEntity.class);

        when(apiClient.fetchFinancialDashboard()).thenReturn(financialDashboardResponse);
        when(apiClient.fetchParticipants(any())).thenReturn(participantsDataEntity);

        final BbvaAccountFetcher fetcher = new BbvaAccountFetcher(apiClient);
        final Collection<TransactionalAccount> transactionalAccounts = fetcher.fetchAccounts();

        Assert.assertEquals(3, transactionalAccounts.size());
    }

    @Test
    public void testFetchingParticipants() throws IOException {
        final ParticipantsDataEntity participantsDataEntity =
                loadSampleData("participants.json", ParticipantsDataEntity.class);

        when(apiClient.fetchParticipants(any())).thenReturn(participantsDataEntity);

        Assert.assertEquals(1, participantsDataEntity.getParticipants().size());
    }

    @Test
    public void shouldFetchAccountTransactions() throws IOException {
        final AccountTransactionsResponse transactions =
                loadSampleData("transactions.json", AccountTransactionsResponse.class);

        when(apiClient.fetchAccountTransactions(any(), any())).thenReturn(transactions);

        AccountTransactionsResponse response = apiClient.fetchAccountTransactions(any(), any());

        Assert.assertEquals(1, response.getAccountTransactions().size());
    }

    @Test
    public void shouldThrowHttpResponseException() {
        HttpResponse httpResponse = mockResponse(409);
        HttpResponseException httpResponseException =
                new HttpResponseException(any(), httpResponse);

        when(apiClient.fetchAccountTransactions(any(), null)).thenThrow(httpResponseException);

        Throwable throwable =
                catchThrowable(() -> apiClient.fetchAccountTransactions(any(), any()));

        Assert.assertEquals(throwable, httpResponseException);
    }

    @Test
    public void shouldGetHolderNamesList() {

        when(accountEntity.getParties(any()))
                .thenReturn(
                        Arrays.asList(
                                new Party("OWNER", Role.HOLDER),
                                new Party("AUTH", Role.AUTHORIZED_USER)));

        List<Party> parties = accountEntity.getParties(any());

        Assert.assertEquals("Owner", parties.get(0).getName());
        Assert.assertEquals(Role.HOLDER, parties.get(0).getRole());
        Assert.assertEquals("Auth", parties.get(1).getName());
        Assert.assertEquals(Role.AUTHORIZED_USER, parties.get(1).getRole());
    }

    @Test
    public void shouldAddHolderNameList() {
        when(accountEntity.toTinkTransactionalAccount(any())).thenReturn(transactionalAccount);

        Assert.assertEquals(
                "Owner",
                accountEntity
                        .toTinkTransactionalAccount(any())
                        .get()
                        .getParties()
                        .get(0)
                        .getName());
        Assert.assertEquals(
                Role.HOLDER,
                accountEntity
                        .toTinkTransactionalAccount(any())
                        .get()
                        .getParties()
                        .get(0)
                        .getRole());
    }

    @Test
    public void shouldGetProducts() throws IOException {
        final ProductEntity product = loadSampleData("product.json", ProductEntity.class);
        when(accountEntity.getAccountProductId()).thenReturn(product.getId());

        Assert.assertEquals("0CA0000079", accountEntity.getAccountProductId());
    }

    @Test
    public void shouldGetEmptyProducts() throws IOException {
        final ProductEntity product = loadSampleData("product_null.json", ProductEntity.class);
        when(accountEntity.getAccountProductId()).thenReturn(product.getId());

        Assert.assertEquals(null, accountEntity.getAccountProductId());
    }

    @Test
    public void shouldGetIban() {
        when(accountEntity.getAccountNumber()).thenReturn("ES02 0182 1048 6000 0000 0000");

        Assert.assertEquals(
                transactionalAccount.get().getAccountNumber(), accountEntity.getAccountNumber());
    }

    @Test
    public void shouldGetEmptyIban() {
        when(accountEntity.getAccountNumber()).thenReturn(null);

        Assert.assertEquals(null, accountEntity.getAccountNumber());
    }

    @Test
    public void shouldFetchAccountDateTransactions() throws IOException {
        final AccountTransactionsResponse transactions =
                loadSampleData("date_transactions.json", AccountTransactionsResponse.class);

        when(apiClient.fetchAccountTransactions(any(), any())).thenReturn(transactions);

        AccountTransactionsResponse response = apiClient.fetchAccountTransactions(any(), any());

        Assert.assertEquals(2, response.getPagination().getNumPages());
        Assert.assertTrue(response.getPagination().getNextPage(), true);
    }

    @Test
    public void shouldFetchAccountDateTransactionsWithPagination() throws IOException {
        final AccountTransactionsResponse transactions =
                loadSampleData("date_transactions.json", AccountTransactionsResponse.class);
        final AccountTransactionsResponse transactionsNextPage =
                loadSampleData(
                        "date_transactions_next_page.json", AccountTransactionsResponse.class);

        when(apiClient.fetchAccountTransactions(any(), any())).thenReturn(transactions);
        AccountTransactionsResponse response = apiClient.fetchAccountTransactions(any(), any());
        when(apiClient.fetchAccountTransactions(any(), any())).thenReturn(transactionsNextPage);

        AccountTransactionsResponse newResponse = null;
        if (!response.getPagination().getNextPage().isEmpty()) {
            newResponse = apiClient.fetchAccountTransactions(any(), any());
        }

        assert newResponse != null;
        Assert.assertEquals(2, newResponse.getPagination().getNumPages());
        Assert.assertEquals(1, newResponse.getAccountTransactions().size());
        Assert.assertTrue(Strings.isNullOrEmpty(newResponse.getPagination().getNextPage()));
    }

    @Test
    public void shouldThrowHttpOtpResponseException() throws IOException {
        HttpResponse httpResponse = mockResponse(401, "otp_error.json");
        HttpResponseException httpResponseException = new HttpResponseException(null, httpResponse);

        when(apiClient.fetchAccountTransactions(any(), any())).thenThrow(httpResponseException);

        Throwable throwable =
                catchThrowable(() -> apiClient.fetchAccountTransactions(any(), any()));

        Assert.assertEquals(throwable, httpResponseException);
    }

    @Test
    public void shouldThrowUnknownHttpResponseException() throws IOException {
        HttpResponse httpResponse = mockResponse(500, "service_unavailable.json");
        HttpResponseException httpResponseException = new HttpResponseException(null, httpResponse);

        when(apiClient.fetchAccountTransactions(any(), any())).thenThrow(httpResponseException);

        Throwable throwable =
                catchThrowable(() -> apiClient.fetchAccountTransactions(any(), any()));

        BbvaErrorResponse error =
                httpResponseException.getResponse().getBody(BbvaErrorResponse.class);
        Assert.assertEquals(throwable, httpResponseException);
        Assert.assertEquals(500, error.getHttpStatus());
        Assert.assertEquals("451", error.getErrorCode());
        Assert.assertEquals(
                "Error al recuperar la información de perfilado del servicio",
                error.getErrorMessage());
    }

    private HttpResponse mockResponse(int status, String path) throws IOException {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(status);
        when(mocked.getBody(BbvaErrorResponse.class))
                .thenReturn(loadSampleData(path, BbvaErrorResponse.class));
        return mocked;
    }

    private HttpResponse mockResponse(int status) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(status);
        return mocked;
    }

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new VavrModule());
        return objectMapper.readValue(Paths.get(DATA_PATH, path).toFile(), cls);
    }
}
