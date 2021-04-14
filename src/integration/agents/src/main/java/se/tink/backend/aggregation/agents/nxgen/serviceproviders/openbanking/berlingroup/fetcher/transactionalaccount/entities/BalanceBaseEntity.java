package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.Accounts;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMappable;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceBaseEntity implements BalanceMappable {
    private BalanceAmountBaseEntity balanceAmount;
    private String balanceType;
    private Boolean creditLimitIncluded;

    public BalanceAmountBaseEntity getBalanceAmount() {
        return balanceAmount;
    }

    public boolean isClosingBooked() {
        return balanceType.equalsIgnoreCase(Accounts.BALANCE_CLOSING_BOOKED)
                || balanceType.equalsIgnoreCase(Accounts.CLBD);
    }

    public boolean isInCurrency(final String currency) {
        return balanceAmount.getCurrency().equalsIgnoreCase(currency);
    }

    @Override
    public ExactCurrencyAmount toTinkAmount() {
        return balanceAmount.toAmount();
    }

    @Override
    public boolean isCreditLimitIncluded() {
        return Optional.ofNullable(creditLimitIncluded).orElse(false);
    }

    @Override
    public Optional<BalanceType> getBalanceType() {
        return BalanceType.findByStringType(balanceType);
    }
}
