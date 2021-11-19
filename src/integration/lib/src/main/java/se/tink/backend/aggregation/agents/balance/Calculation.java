package se.tink.backend.aggregation.agents.balance;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@FunctionalInterface
public interface Calculation {

    Optional<ExactCurrencyAmount> evaluate(
            Pair<ExactCurrencyAmount, Instant> balanceWithSnapshotTime,
            List<Transaction> transactions);
}
