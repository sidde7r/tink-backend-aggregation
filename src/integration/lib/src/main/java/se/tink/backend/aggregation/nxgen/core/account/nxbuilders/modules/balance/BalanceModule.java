package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance;

import com.google.common.base.Preconditions;
import java.util.Optional;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceStep;
import se.tink.libraries.amount.Amount;

public final class BalanceModule {

    private final Amount balance;
    private final Double interestRate;
    private final Amount availableCredit;

    private BalanceModule(Builder builder) {
        this.balance = builder.balance;
        this.interestRate = builder.interestRate;
        this.availableCredit = builder.availableCredit;
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

    public Optional<Double> getInterestRate() {
        return Optional.ofNullable(interestRate);
    }

    public Optional<Amount> getAvailableCredit() {
        return Optional.ofNullable(availableCredit);
    }

    private static class Builder implements BalanceStep<BalanceBuilderStep>, BalanceBuilderStep {

        private Double interestRate;
        private Amount availableCredit;
        private Amount balance;

        @Override
        public BalanceBuilderStep setInterestRate(double interestRate) {
            this.interestRate = interestRate;
            return this;
        }

        @Override
        public BalanceBuilderStep setAvailableCredit(@Nonnull Amount availableCredit) {
            Preconditions.checkNotNull(balance, "Available Credit must not be null.");
            this.availableCredit = availableCredit;
            return this;
        }

        @Override
        public BalanceBuilderStep withBalance(@Nonnull Amount balance) {
            Preconditions.checkNotNull(balance, "Balance must not be null.");
            this.balance = balance;
            return this;
        }

        @Override
        public BalanceModule build() {
            return new BalanceModule(this);
        }
    }
}
