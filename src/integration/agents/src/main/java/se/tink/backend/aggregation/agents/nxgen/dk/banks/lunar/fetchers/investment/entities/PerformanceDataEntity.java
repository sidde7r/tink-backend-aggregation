package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PerformanceDataEntity {

    private static final int BANKING_ROUNDING_SCALE = 8;
    private static final int MONEY_VALUE_ROUNDING_SCALE = 2;

    private AllPerformanceDataEntity all;

    @JsonIgnore
    public double getTotalProfit(BigDecimal totalValue) {
        // This calculation may be inaccurate since we depend on the fraction returned by the bank,
        // which could be rounded
        if (all == null
                || all.getTotalProfitRateAsDecimal() == null
                || BigDecimal.ZERO.equals(all.getTotalProfitRateAsDecimal())) {
            return 0;
        }
        return countInitialValue(totalValue)
                .subtract(totalValue)
                .negate()
                .setScale(MONEY_VALUE_ROUNDING_SCALE, RoundingMode.HALF_EVEN)
                .doubleValue();
    }

    private BigDecimal countInitialValue(BigDecimal totalValue) {
        return totalValue.divide(
                BigDecimal.ONE.add(all.getTotalProfitRateAsDecimal()),
                BANKING_ROUNDING_SCALE,
                RoundingMode.HALF_EVEN);
    }

    @JsonObject
    @Getter
    private static class AllPerformanceDataEntity {
        @JsonProperty("returnFraction")
        private BigDecimal totalProfitRateAsDecimal;
    }
}
