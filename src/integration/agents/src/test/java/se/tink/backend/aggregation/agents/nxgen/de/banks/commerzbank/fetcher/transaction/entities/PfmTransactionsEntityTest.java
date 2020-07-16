package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PfmTransactionsEntityTest {
    private static final String ENTITY_JSON =
            "{\n"
                    + "    \"amount\": {\n"
                    + "        \"value\": 234.56,\n"
                    + "        \"currency\": \"DKK\"\n"
                    + "    },\n"
                    + "    \"date\": \"2020-04-30\",\n"
                    + "    \"originalText\": \"__TEXT__\",\n"
                    + "    \"uncleared\": true\n"
                    + "}";

    @Test
    public void toTinkTransaction() {
        // given
        PfmTransactionsEntity entity =
                SerializationUtils.deserializeFromString(ENTITY_JSON, PfmTransactionsEntity.class);

        // when
        Transaction result = entity.toTinkTransaction();

        // then
        assertThat(result.getExactAmount()).isEqualTo(ExactCurrencyAmount.of(234.56, "DKK"));
        assertThat(result.getDescription()).isEqualTo("__TEXT__");
        assertThat(result.isPending()).isTrue();
    }
}
