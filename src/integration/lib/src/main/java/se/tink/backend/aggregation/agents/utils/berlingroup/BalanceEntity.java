package se.tink.backend.aggregation.agents.utils.berlingroup;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@Setter
public class BalanceEntity {
    private AmountEntity balanceAmount;
    private String balanceType;
    private Boolean creditLimitIncluded;
    private String lastChangeDateTime;
    private String referenceDate;
    private String lastCommittedTransaction;

    public ExactCurrencyAmount toTinkAmount() {
        return balanceAmount.toTinkAmount();
    }
}
