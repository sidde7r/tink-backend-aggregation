package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import org.junit.Test;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BalancesResponseTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void getBalancesForEmptyBalancesInResponse() {
        // given
        BalancesResponse balancesResponse =
                balancesAsResponse(accountEntityProps(), Collections.emptyList());

        // when
        Throwable result = catchThrowable(balancesResponse::getBalance);

        // then
        assertThat(result)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Account balance not found");
    }

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
        ExactCurrencyAmount expectedAmount = new ExactCurrencyAmount(new BigDecimal("234"), "EUR");

        // when
        ExactCurrencyAmount result = balancesResponse.getBalance();

        // then
        assertThat(result).isEqualTo(expectedAmount);
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
