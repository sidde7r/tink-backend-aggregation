package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.account;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.jackson.datatype.VavrModule;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.account.rpc.CajamarAccountTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CajamarAccountTransactionsFetcherTest {

    static final String DATA_PATH = "data/test/agents/es/cajamar/";
    private CajamarApiClient apiClient;
    private Optional<TransactionalAccount> transactionalAccount;

    @Before
    public void setup() {
        apiClient = mock(CajamarApiClient.class);
        transactionalAccount =
                TransactionalAccount.nxBuilder()
                        .withTypeAndFlagsFrom(CajamarConstants.ACCOUNT_TYPE_MAPPER, "3")
                        .withBalance(BalanceModule.of(ExactCurrencyAmount.of(3133.86, "EUR")))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier("ES2030000000000000000000")
                                        .withAccountNumber("ES2030000000000000000000")
                                        .withAccountName("Cuenta de Jefa")
                                        .addIdentifier(
                                                AccountIdentifier.create(
                                                        AccountIdentifierType.IBAN,
                                                        "ES2030000000000000000000"))
                                        .build())
                        .setApiIdentifier("A1")
                        .addParties(new Party("ADRIAN DE LA PACO", Role.HOLDER))
                        .build();
    }

    @Test
    public void shouldFetchAccountTransactions() throws IOException {
        // given
        final CajamarAccountTransactionsResponse accountTransactionsResponse =
                loadSampleData(
                        "account_transactions.json", CajamarAccountTransactionsResponse.class);
        when(apiClient.fetchAccountTransactions(transactionalAccount.get(), null))
                .thenReturn(accountTransactionsResponse);

        // when
        final CajamarAccountTransactionFetcher fetcher =
                new CajamarAccountTransactionFetcher(apiClient);
        final TransactionKeyPaginatorResponse<String> transactions =
                fetcher.getTransactionsFor(transactionalAccount.get(), null);

        // then
        Assert.assertEquals(2, transactions.getTinkTransactions().size());
    }

    @Test
    public void shouldFetchNullOrEmptyAccountTransactions() throws IOException {
        // given
        final CajamarAccountTransactionsResponse accountTransactionsResponse =
                loadSampleData(
                        "account_empty_transactions.json",
                        CajamarAccountTransactionsResponse.class);
        when(apiClient.fetchAccountTransactions(transactionalAccount.get(), null))
                .thenReturn(accountTransactionsResponse);

        // when
        final CajamarAccountTransactionFetcher fetcher =
                new CajamarAccountTransactionFetcher(apiClient);
        final TransactionKeyPaginatorResponse<String> transactions =
                fetcher.getTransactionsFor(transactionalAccount.get(), null);

        // then
        Assert.assertEquals(0, transactions.getTinkTransactions().size());
    }

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new VavrModule());
        return objectMapper.readValue(Paths.get(DATA_PATH, path).toFile(), cls);
    }
}
