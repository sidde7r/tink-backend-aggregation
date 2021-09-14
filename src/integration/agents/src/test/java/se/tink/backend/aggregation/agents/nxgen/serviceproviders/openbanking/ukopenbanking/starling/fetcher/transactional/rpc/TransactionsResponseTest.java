package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.starling.fetcher.transactional.rpc;

import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.file.Paths;
import java.util.Collection;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class TransactionsResponseTest {

    static final String DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/starling/resources/";

    @Test
    public void shouldFilterOutIrrelevantTransactions() {
        // given
        final TransactionsResponse response =
                SerializationUtils.deserializeFromString(
                        Paths.get(DATA_PATH, "transactions.json").toFile(),
                        new TypeReference<TransactionsResponse>() {});

        // when
        Collection<? extends Transaction> tinkTransactions = response.getTinkTransactions();

        // then
        Assertions.assertThat(tinkTransactions.size()).isEqualTo(3);
    }
}
