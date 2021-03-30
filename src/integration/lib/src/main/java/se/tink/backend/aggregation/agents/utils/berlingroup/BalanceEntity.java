package se.tink.backend.aggregation.agents.utils.berlingroup;

import java.util.Optional;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Setter
@JsonObject
@NoArgsConstructor
@Accessors(chain = true)
public class BalanceEntity implements BalanceMappable {
    private AmountEntity balanceAmount;
    private String balanceType;
    private Boolean creditLimitIncluded;
    private String lastChangeDateTime;
    private String referenceDate;
    private String lastCommittedTransaction;

    @Override
    public boolean isCreditLimitIncluded() {
        return Optional.ofNullable(creditLimitIncluded).orElse(false);
    }

    @Override
    public ExactCurrencyAmount toTinkAmount() {
        return balanceAmount.toTinkAmount();
    }

    @Override
    public Optional<BalanceType> getBalanceType() {
        return BalanceType.findByStringType(balanceType);
    }
}
