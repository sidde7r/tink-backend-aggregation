package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.Accounts;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMappable;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity implements BalanceMappable {
    private String balanceType;
    private BalanceAmountEntity balanceAmount;

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase(Accounts.AVAILABLE_BALANCE);
    }

    public ExactCurrencyAmount toAmount() {
        return Optional.ofNullable(balanceAmount)
                .map(balance -> new ExactCurrencyAmount(balance.getAmount(), balance.getCurrency()))
                .orElseThrow(() -> new IllegalStateException("Could not parse amount"));
    }

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
