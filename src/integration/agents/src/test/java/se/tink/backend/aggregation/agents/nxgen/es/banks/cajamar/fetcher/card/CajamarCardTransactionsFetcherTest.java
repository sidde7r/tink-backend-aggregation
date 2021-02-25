package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.card;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.jackson.datatype.VavrModule;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.CajamarCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.rpc.CajamarCreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CajamarCardTransactionsFetcherTest {
    static final String DATA_PATH = "data/test/agents/es/cajamar/";
    private CajamarApiClient apiClient;
    private CreditCardAccount creditCardAccount;

    @Before
    public void setup() {
        apiClient = mock(CajamarApiClient.class);
        creditCardAccount =
                CreditCardAccount.nxBuilder()
                        .withCardDetails(
                                CreditCardModule.builder()
                                        .withCardNumber("4150********1111")
                                        .withBalance(ExactCurrencyAmount.of(3133.86, "EUR"))
                                        .withAvailableCredit(ExactCurrencyAmount.of(3133.86, "EUR"))
                                        .withCardAlias("VISA DEBITO")
                                        .build())
                        .withInferredAccountFlags()
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier("1")
                                        .withAccountNumber("ES20 3058 0000 0000 0000 0000")
                                        .withAccountName("")
                                        .addIdentifier(
                                                AccountIdentifier.create(
                                                        AccountIdentifierType.OTHER, "A1"))
                                        .build())
                        .addParties(new Party("ADRIAN DE LA PACO", Role.HOLDER))
                        .setApiIdentifier("A1")
                        .build();
    }

    @Test
    public void shouldFetchCajamarCardTransactions() throws IOException {
        // given
        final CajamarCreditCardTransactionsResponse cardTransactions =
                loadSampleData(
                        "card_transactions.json", CajamarCreditCardTransactionsResponse.class);
        when(apiClient.fetchCreditCardTransactions(creditCardAccount, null))
                .thenReturn(cardTransactions);

        // when
        final CajamarCreditCardTransactionFetcher cajamarCardFetcher =
                new CajamarCreditCardTransactionFetcher(apiClient);
        final TransactionKeyPaginatorResponse<String> transactions =
                cajamarCardFetcher.getTransactionsFor(creditCardAccount, null);

        // then
        Assert.assertEquals(2, transactions.getTinkTransactions().size());
    }

    @Test
    public void shouldFetchNullOrEmptyCajamarCardTransactions() throws IOException {
        // given
        final CajamarCreditCardTransactionsResponse cardTransactions =
                loadSampleData(
                        "card_empty_transactions.json",
                        CajamarCreditCardTransactionsResponse.class);
        when(apiClient.fetchCreditCardTransactions(creditCardAccount, null))
                .thenReturn(cardTransactions);

        // when
        final CajamarCreditCardTransactionFetcher cajamarCardFetcher =
                new CajamarCreditCardTransactionFetcher(apiClient);
        final TransactionKeyPaginatorResponse<String> transactions =
                cajamarCardFetcher.getTransactionsFor(creditCardAccount, null);

        // then
        Assert.assertEquals(0, transactions.getTinkTransactions().size());
    }

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new VavrModule());
        return objectMapper.readValue(Paths.get(DATA_PATH, path).toFile(), cls);
    }
}
