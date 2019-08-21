package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance;

import com.google.common.base.Preconditions;
import java.util.Optional;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceStep;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public final class BalanceModule {

    private final ExactCurrencyAmount exactBalance;
    private final Double interestRate;
    private final ExactCurrencyAmount exactAvailableCredit;

    private BalanceModule(Builder builder) {
        this.interestRate = builder.interestRate;
        this.exactAvailableCredit = builder.exactAvailableCredit;
        this.exactBalance = builder.exactBalance;
    }

    public static BalanceStep<BalanceBuilderStep> builder() {
        return new Builder();
    }

    @Deprecated
    public static BalanceModule of(Amount amount) {
        return builder().withBalance(amount).build();
    }

    public static BalanceModule of(ExactCurrencyAmount amount) {
        return builder().withBalance(amount).build();
    }

    @Deprecated
    public Amount getBalance() {
        return new Amount(exactBalance.getCurrencyCode(), exactBalance.getDoubleValue());
    }

    public ExactCurrencyAmount getExactBalance() {
        return exactBalance;
    }

    public Optional<Double> getInterestRate() {
        return Optional.ofNullable(interestRate);
    }

    public Optional<Amount> getAvailableCredit() {
        return Optional.ofNullable(exactAvailableCredit)
                .map(e -> new Amount(e.getCurrencyCode(), e.getDoubleValue()));
    }

    public Optional<ExactCurrencyAmount> getExactAvaliableCredit() {
        return Optional.ofNullable(this.exactAvailableCredit);
    }

    private static class Builder implements BalanceStep<BalanceBuilderStep>, BalanceBuilderStep {

        private Double interestRate;
        private ExactCurrencyAmount exactAvailableCredit;
        private ExactCurrencyAmount exactBalance;

        @Override
        public BalanceBuilderStep setInterestRate(double interestRate) {
            Preconditions.checkArgument(interestRate >= 0, "Interest rate must not be negative.");
            this.interestRate = interestRate;
            return this;
        }

        @Deprecated
        @Override
        public BalanceBuilderStep setAvailableCredit(@Nonnull Amount availableCredit) {
            Preconditions.checkNotNull(availableCredit, "Available Credit must not be null.");
            this.exactAvailableCredit =
                    ExactCurrencyAmount.of(
                            availableCredit.toBigDecimal(), availableCredit.getCurrency());
            return this;
        }

        @Override
        public BalanceBuilderStep setAvailableCredit(@Nonnull ExactCurrencyAmount availableCredit) {
            Preconditions.checkNotNull(availableCredit, "Available Credit must not be null.");
            this.exactAvailableCredit = availableCredit;
            return this;
        }

        @Deprecated
        @Override
        public BalanceBuilderStep withBalance(@Nonnull Amount balance) {
            Preconditions.checkNotNull(balance, "Balance must not be null.");
            this.exactBalance =
                    ExactCurrencyAmount.of(balance.toBigDecimal(), balance.getCurrency());
            return this;
        }

        @Override
        public BalanceBuilderStep withBalance(@Nonnull ExactCurrencyAmount balance) {
            Preconditions.checkNotNull(balance, "Balance must not be null.");
            this.exactBalance = balance;
            return this;
        }

        @Override
        public BalanceModule build() {
            return new BalanceModule(this);
        }
    }
}
