package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.Properties;
import org.junit.Test;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class AmountEntityTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void toTinkAmount() {
        // given
        AmountEntity entity = amountAsJson(amountEntityProps());

        // when
        ExactCurrencyAmount result = entity.toTinkAmount();

        // then
        assertThat(result).isEqualTo(new ExactCurrencyAmount(new BigDecimal("123.45"), "EUR"));
    }

    private Properties amountEntityProps() {
        Properties amount = new Properties();
        amount.setProperty("amount", "123.45");
        amount.setProperty("currency", "EUR");
        return amount;
    }

    private static AmountEntity amountAsJson(final Properties transaction) {
        Gson gsonObj = new Gson();
        try {
            return OBJECT_MAPPER.readValue(gsonObj.toJson(transaction), AmountEntity.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
