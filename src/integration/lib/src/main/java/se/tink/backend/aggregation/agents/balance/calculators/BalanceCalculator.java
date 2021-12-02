package se.tink.backend.aggregation.agents.balance.calculators;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.backend.aggregation.agents.balance.Calculation;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@RequiredArgsConstructor
public class BalanceCalculator {

    public Optional<ExactCurrencyAmount> findFirstPossibleCalculationAndEvaluateIt(
            Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances,
            List<Transaction> transactions,
            List<Pair<AccountBalanceType, Calculation>> prioritizedCalculations) {

        Optional<Pair<AccountBalanceType, Calculation>> balanceTypeWithCalculation =
                findFirstPossibleCalculation(granularBalances, prioritizedCalculations);

        if (!balanceTypeWithCalculation.isPresent()) {
            log.info(
                    "[BALANCE CALCULATOR] Not found any possible calculations for given balances: {}",
                    granularBalances.keySet());
            return Optional.empty();
        }

        AccountBalanceType balanceType = balanceTypeWithCalculation.get().getLeft();
        Calculation calculation = balanceTypeWithCalculation.get().getRight();
        Pair<ExactCurrencyAmount, Instant> balanceWithSnapshotTime =
                granularBalances.get(balanceType);

        log.info(
                "[BALANCE CALCULATOR] Running calculation with balance {} {} as input",
                balanceType,
                balanceWithSnapshotTime.getLeft());

        return calculation.evaluate(balanceWithSnapshotTime, transactions);
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
