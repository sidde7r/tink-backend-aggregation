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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ParticipantsDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ProductEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.FinancialDashboardResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder.Role;
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
                                        .withAccountName("CUENTAS PERSONALES")
                                        .addIdentifier(
                                                AccountIdentifier.create(
                                                        Type.IBAN, "ES0201821048600000000000"))
                                        .build())
                        .setApiIdentifier("ES0XXXXXXXXXXXXXXXXXXXXXXXXXXX")
                        .addHolders(Arrays.asList(Holder.of("OWNER", Role.HOLDER)))
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

        Assert.assertEquals(response.getAccountTransactions().size(), 1);
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

        when(accountEntity.getHolders(any()))
                .thenReturn(
                        Arrays.asList(
                                Holder.of("OWNER", Role.HOLDER),
                                Holder.of("AUTH", Role.AUTHORIZED_USER)));

        List<Holder> holders = accountEntity.getHolders(any());

        Assert.assertEquals(holders.get(0).getName(), "OWNER");
        Assert.assertEquals(holders.get(0).getRole(), Role.HOLDER);
        Assert.assertEquals(holders.get(1).getName(), "AUTH");
        Assert.assertEquals(holders.get(1).getRole(), Role.AUTHORIZED_USER);
    }

    @Test
    public void shouldAddHolderNameList() {
        when(accountEntity.toTinkTransactionalAccount(any())).thenReturn(transactionalAccount);

        Assert.assertEquals(
                accountEntity.toTinkTransactionalAccount(any()).get().getHolders().get(0).getName(),
                "OWNER");
        Assert.assertEquals(
                accountEntity.toTinkTransactionalAccount(any()).get().getHolders().get(0).getRole(),
                Role.HOLDER);
    }

    @Test
    public void shouldGetProducts() throws IOException {
        final ProductEntity product = loadSampleData("product.json", ProductEntity.class);
        when(accountEntity.getAccountProductId()).thenReturn(product.getId());

        Assert.assertEquals(accountEntity.getAccountProductId(), "0CA0000079");
    }

    @Test
    public void shouldGetEmptyProducts() throws IOException {
        final ProductEntity product = loadSampleData("product_null.json", ProductEntity.class);
        when(accountEntity.getAccountProductId()).thenReturn(product.getId());

        Assert.assertEquals(accountEntity.getAccountProductId(), null);
    }

    @Test
    public void shouldGetIban() {
        when(accountEntity.getAccountNumber()).thenReturn("ES02 0182 1048 6000 0000 0000");

        Assert.assertEquals(
                accountEntity.getAccountNumber(), transactionalAccount.get().getAccountNumber());
    }

    @Test
    public void shouldGetEmptyIban() {
        when(accountEntity.getAccountNumber()).thenReturn(null);

        Assert.assertEquals(accountEntity.getAccountNumber(), null);
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
