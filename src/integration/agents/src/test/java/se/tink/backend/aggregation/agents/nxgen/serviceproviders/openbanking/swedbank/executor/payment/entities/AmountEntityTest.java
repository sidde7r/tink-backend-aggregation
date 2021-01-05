package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.entities;

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
    public void verifyThatAmountIsNotModifiedWhenDeserializing() {
        // given
        AmountEntity entity = amountAsJson(getAmountEntityProperties());

        // when
        ExactCurrencyAmount result = entity.toTinkAmount();

        // then
        assertThat(result).isEqualTo(new ExactCurrencyAmount(new BigDecimal("99.99"), "SEK"));
    }

    private Properties getAmountEntityProperties() {
        Properties amount = new Properties();
        amount.setProperty("amount", "99.99");
        amount.setProperty("currency", "SEK");
        return amount;
    }

    private static AmountEntity amountAsJson(final Properties amount) {
        Gson gsonObj = new Gson();
        try {
            return OBJECT_MAPPER.readValue(gsonObj.toJson(amount), AmountEntity.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
