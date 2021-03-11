package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PerformanceDataEntity {

    private AllPerformanceDataEntity all;

    @JsonIgnore
    public double getTotalProfit() {
        if (all == null || all.getCumulativeProfitLoss() == null) {
            return 0;
        }
        return all.getCumulativeProfitLoss().setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }

    @JsonObject
    @Getter
    private static class AllPerformanceDataEntity {
        private BigDecimal cumulativeProfitLoss;
    }
}
