package se.tink.backend.aggregation.agents.balance.calculators;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.backend.aggregation.agents.balance.CalculationSummary;

@Builder
public class BalanceCalculatorSummary {
    private final AccountBalanceType inputBalanceType;
    private final BigDecimal inputBalance;
    private final LocalDateTime inputBalanceSnapshotTime;
    private final CalculationSummary calculationSummary;
}
