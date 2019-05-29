package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance;

import com.google.common.base.Preconditions;
import java.util.Optional;
import javax.annotation.Nonnull;
import se.tink.backend.agents.rpc.ExactCurrencyAmount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceStep;
import se.tink.libraries.amount.Amount;

public final class BalanceModule {

    private final Amount balance;
    private final ExactCurrencyAmount exactBalance;
    private final Double interestRate;
    private final Amount availableCredit;
    private final ExactCurrencyAmount exactAvailableCredit;

    private BalanceModule(Builder builder) {
        this.balance = builder.balance;
        this.interestRate = builder.interestRate;
        this.availableCredit = builder.availableCredit;
        this.exactAvailableCredit = builder.exactAvailableCredit;
        this.exactBalance = builder.exactBalance;
    }

    public static BalanceStep<BalanceBuilderStep> builder() {
        return new Builder();
    }

    public static BalanceModule of(Amount amount) {
        return builder().withBalance(amount).build();
    }

     public Amount getBalance() {
        return new Amount(balance.getCurrency(), balance.getValue());
    }

    public ExactCurrencyAmount getExactBalance() {
        return ExactCurrencyAmount.of(this.exactBalance);
    }

    public Optional<Double> getInterestRate() {
        return Optional.ofNullable(interestRate);
    }

    public Optional<Amount> getAvailableCredit() {
        return Optional.ofNullable(availableCredit);
    }
    public Optional<ExactCurrencyAmount> getExactAvaliableCredit() {
        return Optional.ofNullable(this.exactAvailableCredit);
    }
    private static class Builder implements BalanceStep<BalanceBuilderStep>, BalanceBuilderStep {

        private Double interestRate;
        private Amount availableCredit;
        private ExactCurrencyAmount exactAvailableCredit;
        private Amount balance;
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
            this.availableCredit =
                    new Amount(availableCredit.getCurrency(), availableCredit.getValue());
            this.exactAvailableCredit =
                    ExactCurrencyAmount.of(
                            availableCredit.getValue(), availableCredit.getCurrency());
            return this;
        }

        @Override
        public BalanceBuilderStep setAvailableCredit(@Nonnull ExactCurrencyAmount availableCredit) {
            Preconditions.checkNotNull(availableCredit, "Available Credit must not be null.");
            this.availableCredit =
                    new Amount(availableCredit.getCurrencyCode(), availableCredit.getDoubleValue());
            this.exactAvailableCredit = ExactCurrencyAmount.of(availableCredit);
            return this;
        }

        @Override
        public BalanceBuilderStep withBalance(@Nonnull Amount balance) {
            Preconditions.checkNotNull(balance, "Balance must not be null.");
            this.balance = new Amount(balance.getCurrency(), balance.getValue());
            this.exactBalance =
                    ExactCurrencyAmount.of(balance.doubleValue(), balance.getCurrency());
            return this;
        }

        @Override
        public BalanceBuilderStep withBalance(@Nonnull ExactCurrencyAmount balance) {
            Preconditions.checkNotNull(balance, "Balance must not be null.");
            this.balance = new Amount(balance.getCurrencyCode(), balance.getDoubleValue());
            this.exactBalance = ExactCurrencyAmount.of(balance);
            return this;
        }

        @Override
        public BalanceModule build() {
            return new BalanceModule(this);
        }
    }
}
