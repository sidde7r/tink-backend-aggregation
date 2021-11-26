package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance;

import com.google.common.base.Preconditions;
import java.util.Optional;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceStep;
import se.tink.libraries.amount.ExactCurrencyAmount;

public final class BalanceModule {

    private final ExactCurrencyAmount exactBalance;
    private final Double interestRate;
    private final ExactCurrencyAmount exactAvailableCredit;
    private ExactCurrencyAmount exactAvailableBalance;
    private ExactCurrencyAmount exactCreditLimit;

    private BalanceModule(Builder builder) {
        this.interestRate = builder.interestRate;
        this.exactAvailableCredit = builder.exactAvailableCredit;
        this.exactBalance = builder.exactBalance;
        this.exactAvailableBalance = builder.exactAvailableBalance;
        this.exactCreditLimit = builder.exactCreditLimit;
    }

    public static BalanceStep<BalanceBuilderStep> builder() {
        return new Builder();
    }

    public static BalanceModule of(ExactCurrencyAmount amount) {
        return builder().withBalance(amount).build();
    }

    public ExactCurrencyAmount getExactBalance() {
        return exactBalance;
    }

    public Optional<Double> getInterestRate() {
        return Optional.ofNullable(interestRate);
    }

    public Optional<ExactCurrencyAmount> getExactAvailableCredit() {
        return Optional.ofNullable(this.exactAvailableCredit);
    }

    public ExactCurrencyAmount getExactAvailableBalance() {
        return exactAvailableBalance;
    }

    public ExactCurrencyAmount getExactCreditLimit() {
        return exactCreditLimit;
    }

    private static class Builder implements BalanceStep<BalanceBuilderStep>, BalanceBuilderStep {

        private Double interestRate;
        private ExactCurrencyAmount exactAvailableCredit;
        private ExactCurrencyAmount exactBalance;
        private ExactCurrencyAmount exactAvailableBalance;
        private ExactCurrencyAmount exactCreditLimit;

        @Override
        public BalanceBuilderStep setInterestRate(double interestRate) {
            Preconditions.checkArgument(interestRate >= 0, "Interest rate must not be negative.");
            this.interestRate = interestRate;
            return this;
        }

        @Override
        public BalanceBuilderStep setAvailableCredit(@Nonnull ExactCurrencyAmount availableCredit) {
            Preconditions.checkNotNull(availableCredit, "Available Credit must not be null.");
            this.exactAvailableCredit = availableCredit;
            return this;
        }

        @Override
        public BalanceBuilderStep withBalance(@Nonnull ExactCurrencyAmount balance) {
            Preconditions.checkNotNull(balance, "Balance must not be null.");
            this.exactBalance = balance;
            return this;
        }

        @Override
        public BalanceBuilderStep setAvailableBalance(
                @Nonnull ExactCurrencyAmount availableBalance) {
            Preconditions.checkNotNull(availableBalance, "Available Balance must not be null.");
            this.exactAvailableBalance = availableBalance;
            return this;
        }

        @Override
        public BalanceBuilderStep setCreditLimit(@Nonnull ExactCurrencyAmount creditLimit) {
            Preconditions.checkNotNull(creditLimit, "Credit Limit must not be null.");
            Preconditions.checkArgument(
                    creditLimit.getDoubleValue() >= 0, "Credit Limit must not be negative.");
            this.exactCreditLimit = creditLimit;
            return this;
        }

        @Override
        public BalanceModule build() {
            return new BalanceModule(this);
        }
    }
}
