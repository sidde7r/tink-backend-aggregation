package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LuminorTransactionsFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/luminor/fetcher/transactions/resources";

    @Test
    public void shouldMapToTinkTransaction() {

        // given
        TransactionsResponse transactionsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "transactions_response.json").toFile(),
                        TransactionsResponse.class);
        // when
        Transaction result = (Transaction) transactionsResponse.getTinkTransactions().get(0);

        // then
        assertThat(result)
                .isEqualToComparingFieldByFieldRecursively(getExpectedAccountsResponse().get(0));
    }

    private List<Transaction> getExpectedAccountsResponse() {
        return Collections.singletonList(
                Transaction.builder()
                        .setDate(LocalDate.of(2020, 3, 15))
                        .setAmount(new ExactCurrencyAmount(BigDecimal.valueOf(152.45), "EUR"))
                        .setDescription("Invoice x29876")
                        .build());
    }
}
