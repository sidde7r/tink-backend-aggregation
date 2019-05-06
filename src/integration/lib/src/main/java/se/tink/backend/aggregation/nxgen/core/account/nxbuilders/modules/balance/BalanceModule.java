package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceStep;
import se.tink.libraries.amount.Amount;

public class BalanceModule {

    private final Amount balance;
    private final double interestRate;
    private final Amount availableCredit;

    private BalanceModule(Builder builder) {
        this.balance = builder.balance;
        this.interestRate = builder.interestRate;
        this.availableCredit = builder.availableCredit;
    }

    private BalanceModule(Amount balance) {
        this.balance = balance;
        this.interestRate = 0;
        this.availableCredit = null;
    }

    public static BalanceStep<BalanceBuilderStep> builder() {
        return new Builder();
    }

    public static BalanceModule of(Amount amount) {
        return new BalanceModule(amount);
    }

    public Amount getBalance() {
        return balance;
    }

    // Backwards compatibility
    public Amount getValue() {
        return getBalance();
    }

    public double getInterestRate() {
        return interestRate;
    }

    public Amount getAvailableCredit() {
        return availableCredit;
    }

    private static class Builder implements BalanceStep<BalanceBuilderStep>, BalanceBuilderStep {

        private double interestRate;
        private Amount availableCredit;
        private Amount balance;

        @Override
        public BalanceBuilderStep setInterestRate(double interestRate) {
            this.interestRate = interestRate;
            return this;
        }

        @Override
        public BalanceBuilderStep setAvailableCredit(Amount availableCredit) {
            this.availableCredit = availableCredit;
            return this;
        }

        @Override
        public BalanceBuilderStep setBalance(@Nonnull Amount balance) {
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
