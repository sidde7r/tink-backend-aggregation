package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransactionResponseTest {
    private static final String DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/evobanco/resources";

    @Test
    public void canFetchMore_should_return_false_if_no_more_data_to_fetch() {
        // given
        TransactionsResponse response =
                SerializationUtils.deserializeFromString(
                        Paths.get(DATA_PATH, "transactionResponse_with_no_more_data_to_fetch.json")
                                .toFile(),
                        TransactionsResponse.class);

        // when
        Optional<Boolean> canFetchMore = response.canFetchMore();

        // then
        assertThat(canFetchMore.get()).isFalse();
    }

    @Test
    public void canFetchMore_should_return_true_if_more_data_to_fetch() {
        // given
        TransactionsResponse response =
                SerializationUtils.deserializeFromString(
                        Paths.get(DATA_PATH, "transactionResponse_with_more_data_to_fetch.json")
                                .toFile(),
                        TransactionsResponse.class);

        // when
        Optional<Boolean> canFetchMore = response.canFetchMore();

        // then
        assertThat(canFetchMore.get()).isTrue();
    }
}
