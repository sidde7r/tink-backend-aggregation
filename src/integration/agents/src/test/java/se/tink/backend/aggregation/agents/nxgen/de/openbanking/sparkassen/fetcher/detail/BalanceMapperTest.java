package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.detail;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.FetcherTestData;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchBalancesResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BalanceMapperTest {

    private static final String CURRENCY = "EUR";

    @Test
    public void shouldReturnZeroInProvidedCurrencyWhenNoBalanceReturnedFromBank() {
        // then
        FetchBalancesResponse fetchBalancesResponse = FetcherTestData.NO_BALANCES;
        // when
        ExactCurrencyAmount tinkAmount =
                BalanceMapper.getAvailableBalance(fetchBalancesResponse, CURRENCY);

        // then
        assertThat(tinkAmount).isEqualTo(ExactCurrencyAmount.of(0.0, CURRENCY));
    }

    @Test
    public void shouldReturnZeroInProvidedCurrencyWhenNullListOfBalancesReturnedFromBank() {
        // given
        FetchBalancesResponse fetchBalancesResponse = FetcherTestData.NULL_BALANCES;

        // when
        ExactCurrencyAmount tinkAmount =
                BalanceMapper.getAvailableBalance(fetchBalancesResponse, CURRENCY);

        // then
        assertThat(tinkAmount).isEqualTo(ExactCurrencyAmount.of(0.0, CURRENCY));
    }

    @Test
    public void shouldReturnAmountEqualToFirstElementInBalancesList() {
        // given
        FetchBalancesResponse fetchBalancesResponse =
                FetcherTestData.getFetchBalancesResponse(
                        CURRENCY,
                        BigDecimal.valueOf(102.23),
                        BigDecimal.valueOf(1203.42),
                        BigDecimal.valueOf(1203.22));

        // when
        ExactCurrencyAmount tinkAmount =
                BalanceMapper.getAvailableBalance(fetchBalancesResponse, CURRENCY);

        // then
        assertThat(tinkAmount).isEqualTo(ExactCurrencyAmount.of(102.23, CURRENCY));
    }

    @Test
    public void shouldTransformAmountCorrectly() {
        // given
        BalanceEntity balanceEntity =
                FetcherTestData.getBalanceEntity("EUR", BigDecimal.valueOf(1234.56));

        // when
        ExactCurrencyAmount tinkAmount = BalanceMapper.toTinkAmount(balanceEntity);

        // then
        assertThat(tinkAmount).isEqualTo(ExactCurrencyAmount.of(1234.56, "EUR"));
    }
}
