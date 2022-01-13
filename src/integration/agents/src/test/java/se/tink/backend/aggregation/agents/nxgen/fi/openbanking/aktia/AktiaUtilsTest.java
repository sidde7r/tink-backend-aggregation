package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AktiaUtilsTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/aktia/resources";

    public static final String TRANSACTION_JSON = "transaction.json";

    private final Account account = mock(Account.class);

    @Test
    public void shouldRemoveDuplicatedTransactions() {
        // given
        Transaction transactionJson = readFromFile(TRANSACTION_JSON, Transaction.class);

        FetchTransactionsResponse fetchTransactionsResponseExpected =
                getExpectedTransactionsResponse(Collections.singletonList(transactionJson));

        List<Transaction> duplicatedTransactions = new ArrayList<>();
        duplicatedTransactions.add(transactionJson);
        duplicatedTransactions.add(transactionJson);

        FetchTransactionsResponse fetchTransactionsResponse =
                getExpectedTransactionsResponse(duplicatedTransactions);
        // when
        FetchTransactionsResponse result = AktiaUtils.removeDuplicates(fetchTransactionsResponse);
        // then
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("transactions.id")
                .isEqualTo(fetchTransactionsResponseExpected);
    }

    @Test
    public void shouldReturnEmptyListOfTrx() {
        // given
        FetchTransactionsResponse fetchTransactionsResponseExpected =
                getExpectedTransactionsResponse(Collections.emptyList());
        FetchTransactionsResponse fetchTransactionsResponse =
                getExpectedTransactionsResponse(Collections.emptyList());
        // when
        FetchTransactionsResponse result = AktiaUtils.removeDuplicates(fetchTransactionsResponse);
        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(fetchTransactionsResponseExpected);
    }

    private FetchTransactionsResponse getExpectedTransactionsResponse(
            List<Transaction> transactions) {
        return new FetchTransactionsResponse(Collections.singletonMap(account, transactions));
    }

    private static <T> T readFromFile(String filename, Class<T> klass) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, filename).toFile(), klass);
    }
}
