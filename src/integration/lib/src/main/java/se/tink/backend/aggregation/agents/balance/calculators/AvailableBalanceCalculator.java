package se.tink.backend.aggregation.agents.balance.calculators;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface AvailableBalanceCalculator {

    Pair<Optional<ExactCurrencyAmount>, BalanceCalculatorSummary> calculateAvailableBalance(
            Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances,
            List<Transaction> transactions);
}
