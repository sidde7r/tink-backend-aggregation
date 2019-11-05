package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.entity.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RunWith(JUnitParamsRunner.class)
public class AmountEntityTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        mapper.setVisibilityChecker(
                mapper.getSerializationConfig()
                        .getDefaultVisibilityChecker()
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
    }

    private Object[] amountInJsonResponse() {
        return new Object[] {
            new Object[] {"{\"currency\":\"EUR\",\"content\":\"-6.27\"}", "EUR", "-6.27"},
            new Object[] {"{\"currency\":\"PLN\",\"content\":\"0.1\"}", "PLN", "0.1"},
            new Object[] {"{\"currency\":\"EUR\",\"content\":\"-999999.99\"}", "EUR", "-999999.99"},
        };
    }

    @Test
    @Parameters(method = "amountInJsonResponse")
    public void shouldCorrectlyConvertJsonAmountToTinkExactCurrencyAmount(
            String json, String currency, String amount) throws IOException {
        AmountEntity amountEntity = mapper.readValue(json, AmountEntity.class);

        ExactCurrencyAmount exactCurrencyAmount = amountEntity.toTinkAmount();

        assertThat(exactCurrencyAmount)
                .isEqualTo(new ExactCurrencyAmount(new BigDecimal(amount), currency));
    }
}
