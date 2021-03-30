package se.tink.backend.aggregation.agents.utils.berlingroup;

import java.util.Optional;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface BalanceMappable {

    boolean isCreditLimitIncluded();

    ExactCurrencyAmount toTinkAmount();

    Optional<BalanceType> getBalanceType();
}
