package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BalancesResponseTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void getBalancesForNotEmptyBalancesInResponse() {
        // given
        BalancesResponse balancesResponse =
                balancesAsResponse(
                        accountEntityProps(),
                        Arrays.asList(
                                balanceEntityProps("authorised", "123"),
                                balanceEntityProps("expected", "234"),
                                balanceEntityProps("closingBooked", "345")));
        // and
        ExactCurrencyAmount expectedAmount1 = new ExactCurrencyAmount(new BigDecimal("123"), "EUR");
        ExactCurrencyAmount expectedAmount2 = new ExactCurrencyAmount(new BigDecimal("234"), "EUR");
        ExactCurrencyAmount expectedAmount3 = new ExactCurrencyAmount(new BigDecimal("345"), "EUR");

        // when
        List<BalanceEntity> result = balancesResponse.getBalances();

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).toTinkAmount()).isEqualTo(expectedAmount1);
        assertThat(result.get(1).toTinkAmount()).isEqualTo(expectedAmount2);
        assertThat(result.get(2).toTinkAmount()).isEqualTo(expectedAmount3);
    }

    private Properties accountEntityProps() {
        Properties account = new Properties();
        account.setProperty("iban", "test-iban");
        return account;
    }

    private Properties balanceEntityProps(final String balanceType, final String amount) {
        Properties balance = new Properties();
        balance.setProperty("balanceType", balanceType);
        balance.setProperty("referenceDate", "2019-10-10");
        balance.setProperty("lastChangeDateTime", "2019-10-11");

        Properties amountEntity = new Properties();
        amountEntity.setProperty("amount", amount);
        amountEntity.setProperty("currency", "EUR");

        balance.put("balanceAmount", amountEntity);

        return balance;
    }

    private static BalancesResponse balancesAsResponse(
            final Properties account, final Collection<Properties> balances) {
        Gson gsonObj = new Gson();
        try {
            return OBJECT_MAPPER.readValue(
                    "{\"account\":"
                            + gsonObj.toJson(account)
                            + ", \"balances\":"
                            + gsonObj.toJson(balances)
                            + "}",
                    BalancesResponse.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
