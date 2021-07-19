package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.entity;

import java.util.Optional;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.agents.utils.berlingroup.AmountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMappable;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Setter
@JsonObject
@NoArgsConstructor
public class BalancesEntity implements BalanceMappable {

    private AmountEntity balanceAmount;
    private String balanceType;
    private Boolean creditLimitIncluded;

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
