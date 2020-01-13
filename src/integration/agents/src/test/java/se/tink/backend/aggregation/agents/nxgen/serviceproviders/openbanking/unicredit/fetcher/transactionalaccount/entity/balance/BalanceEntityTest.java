package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.balance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.Properties;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BalanceEntityTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void getAmountWithoutBalanceAmountShouldThrowException() {
        // given
        Properties balanceProperties = balanceEntityProps("expected");
        balanceProperties.remove("balanceAmount");
        BalanceEntity entity = balanceAsJson(balanceProperties);

        // when
        Throwable result = Assertions.catchThrowable(entity::getAmount);

        // then
        Assertions.assertThat(result).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void getAmountWithBalance() {
        // given
        BalanceEntity entity = balanceAsJson(balanceEntityProps("expected"));

        // when
        ExactCurrencyAmount result = entity.getAmount();

        // then
        Assertions.assertThat(result)
                .isEqualTo(new ExactCurrencyAmount(new BigDecimal("123.45"), "EUR"));
    }

    @Test
    public void getBalanceMappingPriorityWithNonExistingBalanceTypeThrowsException() {
        // given
        String nonExistingType = "non-existing-type";
        BalanceEntity entity = balanceAsJson(balanceEntityProps(nonExistingType));

        // when
        Throwable result = Assertions.catchThrowable(entity::getBalanceMappingPriority);

        // then
        Assertions.assertThat(result)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "There is balanceType of value '"
                                + nonExistingType
                                + "' defined in Enum se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.balance.BalanceType");
    }

    @Test
    public void getBalanceMappingPriorityWithExistingBalanceType() {
        // given
        BalanceEntity entity = balanceAsJson(balanceEntityProps("expected"));

        // when
        int result = entity.getBalanceMappingPriority();

        // then
        Assertions.assertThat(result).isEqualTo(1);
    }

    private Properties balanceEntityProps(final String balanceType) {

        Properties balance = new Properties();
        balance.setProperty("balanceType", balanceType);
        balance.setProperty("referenceDate", "test-reference-date");
        balance.setProperty("lastChangeDateTime", "test-last-change-date-time");

        Properties amount = new Properties();
        amount.setProperty("amount", "123.45");
        amount.setProperty("currency", "EUR");

        balance.put("balanceAmount", amount);
        return balance;
    }

    private static BalanceEntity balanceAsJson(final Properties balance) {
        Gson gsonObj = new Gson();
        try {
            return OBJECT_MAPPER.readValue(gsonObj.toJson(balance), BalanceEntity.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
