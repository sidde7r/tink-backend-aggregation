package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity;

import java.util.Optional;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMappable;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity implements BalanceMappable {
    private AmountEntity balanceAmount;
    private String balanceType;
    private Boolean creditLimitIncluded;

    @Override
    public boolean isCreditLimitIncluded() {
        return false;
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
