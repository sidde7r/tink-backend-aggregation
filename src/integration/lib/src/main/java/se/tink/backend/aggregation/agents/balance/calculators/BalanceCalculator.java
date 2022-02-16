package se.tink.backend.aggregation.agents.balance.calculators;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.backend.aggregation.agents.balance.Calculation;
import se.tink.backend.aggregation.agents.balance.CalculationSummary;
import se.tink.backend.aggregation.agents.balance.calculators.BalanceCalculatorSummary.BalanceCalculatorSummaryBuilder;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@RequiredArgsConstructor
public class BalanceCalculator {

    public Pair<Optional<ExactCurrencyAmount>, BalanceCalculatorSummary>
            findFirstPossibleCalculationAndEvaluateIt(
                    Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances,
                    List<Transaction> transactions,
                    List<Pair<AccountBalanceType, Calculation>> prioritizedCalculations) {

        Optional<Pair<AccountBalanceType, Calculation>> balanceTypeWithCalculation =
                findFirstPossibleCalculation(granularBalances, prioritizedCalculations);

        if (!balanceTypeWithCalculation.isPresent()) {
            BalanceCalculatorSummary balanceCalculatorSummary =
                    BalanceCalculatorSummary.builder()
                            .calculationSummary(
                                    CalculationSummary.of(
                                            "Not found any possible calculations for given granular balances"))
                            .build();

            return Pair.of(Optional.empty(), balanceCalculatorSummary);
        }

        AccountBalanceType balanceType = balanceTypeWithCalculation.get().getLeft();
        Calculation calculation = balanceTypeWithCalculation.get().getRight();
        Pair<ExactCurrencyAmount, Instant> balanceWithSnapshotTime =
                granularBalances.get(balanceType);

        BalanceCalculatorSummaryBuilder summaryBuilder =
                BalanceCalculatorSummary.builder()
                        .inputBalanceType(balanceType)
                        .inputBalance(balanceWithSnapshotTime.getLeft().getExactValue())
                        .inputBalanceSnapshotTime(
                                LocalDateTime.ofInstant(
                                        balanceWithSnapshotTime.getRight(), ZoneOffset.UTC));

        Pair<Optional<ExactCurrencyAmount>, CalculationSummary> result =
                calculation.evaluate(balanceWithSnapshotTime, transactions);

        Optional<ExactCurrencyAmount> calculatedBalance = result.getLeft();
        BalanceCalculatorSummary summary =
                summaryBuilder.calculationSummary(result.getRight()).build();
        return Pair.of(calculatedBalance, summary);
    }

    public Optional<Pair<AccountBalanceType, Calculation>> findFirstPossibleCalculation(
            Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances,
            List<Pair<AccountBalanceType, Calculation>> prioritizedCalculations) {

        return prioritizedCalculations.stream()
                .filter(
                        balanceTypeWithCalculation ->
                                granularBalances.containsKey(balanceTypeWithCalculation.getLeft()))
                .findFirst();
    }
}
