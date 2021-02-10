package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.TestHelper.mockHttpClient;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.creditcards.Sparebank1CreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.creditcards.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Sparebank1CreditCardTransactionFetcherTest {
    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/sparebank1/resources";

    @Test
    public void getTransactionForShouldReturnTinkTransactions() {
        // given
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        HttpResponse response = mock(HttpResponse.class);
        TinkHttpClient client = mockHttpClient(requestBuilder, response);
        Sparebank1ApiClient apiClient = new Sparebank1ApiClient(client, "dummyBankId");
        Sparebank1CreditCardTransactionFetcher fetcher =
                new Sparebank1CreditCardTransactionFetcher(apiClient);
        when(requestBuilder.get(CreditCardTransactionsResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCE_PATH, "creditcard_transactions_response.json")
                                        .toFile(),
                                CreditCardTransactionsResponse.class));
        CreditCardAccount creditCardAccount = mock(CreditCardAccount.class);

        // when
        CreditCardTransactionsResponse creditCardTransactionsResponse =
                fetcher.getTransactionsFor(creditCardAccount, "dummyKey");
        Collection<CreditCardTransaction> transactions =
                creditCardTransactionsResponse.getTinkTransactions();

        // then
        assertThat(transactions).hasSize(2);
        Iterator<CreditCardTransaction> iterator = transactions.iterator();
        assertTransaction(
                iterator.next(),
                "description1",
                ExactCurrencyAmount.of(BigDecimal.valueOf(2111.25), "NOK"),
                1610362800000L,
                true);
        assertTransaction(
                iterator.next(),
                "description2",
                ExactCurrencyAmount.of(BigDecimal.valueOf(-11.25), "PLN"),
                1609585200000L,
                false);
    }

    private void assertTransaction(
            CreditCardTransaction transaction,
            String description,
            ExactCurrencyAmount amount,
            long date,
            boolean pending) {
        assertThat(transaction.getDescription()).isEqualTo(description);
        assertThat(transaction.getAmount()).isEqualTo(amount);
        assertThat(transaction.isPending()).isEqualTo(pending);
        assertThat(transaction.getDate()).isEqualTo(new Date(date));
    }
}
