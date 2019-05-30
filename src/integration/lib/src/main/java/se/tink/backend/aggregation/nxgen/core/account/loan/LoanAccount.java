package se.tink.backend.aggregation.nxgen.core.account.loan;

import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.ExactCurrencyAmount;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.amount.Amount;

public class LoanAccount extends Account {
    public static final int BIGGER_THAN = 1;
    private final Double interestRate;
    private final LoanDetails details;

    private LoanAccount(Builder<LoanAccount, DefaultLoanBuilder> builder) {
        super(builder);
        this.interestRate = builder.getInterestRate();
        this.details = builder.getDetails();
    }

    public static Builder<?, ?> builder(String uniqueIdentifier) {
        return new DefaultLoanBuilder(uniqueIdentifier);
    }

    @Deprecated
    public static Builder<?, ?> builder(String uniqueIdentifier, Amount balance) {
        return builder(uniqueIdentifier)
                .setExactBalance(ExactCurrencyAmount.of(balance.getValue(), balance.getCurrency()));
    }

    public static Builder<?, ?> builder(String uniqueIdentifier, ExactCurrencyAmount balance) {
        return builder(uniqueIdentifier).setExactBalance(ExactCurrencyAmount.of(balance));
    }

    private static ExactCurrencyAmount ensureNegativeSign(ExactCurrencyAmount amount) {
        Preconditions.checkNotNull(amount);
        Preconditions.checkArgument(
                amount.getExactValue().compareTo(BigDecimal.ZERO) != BIGGER_THAN);
        return amount;
    }

    @Override
    public AccountTypes getType() {
        return AccountTypes.LOAN;
    }

    @Deprecated
    @Override
    public Amount getBalance() {
        return Amount.createFromAmount(super.getBalance()).orElseThrow(NullPointerException::new);
    }

    @Override
    public ExactCurrencyAmount getExactBalance() {
        return Optional.ofNullable(super.getExactBalance())
                .map(ExactCurrencyAmount::of)
                .orElseThrow(NullPointerException::new);
    }

    public Double getInterestRate() {
        return this.interestRate;
    }

    public LoanDetails getDetails() {
        return this.details;
    }

    private static class DefaultLoanBuilder
            extends LoanAccount.Builder<LoanAccount, DefaultLoanBuilder> {

        public DefaultLoanBuilder(String uniqueIdentifier) {
            super(uniqueIdentifier);
        }

        @Override
        protected DefaultLoanBuilder self() {
            return this;
        }

        @Override
        public LoanAccount build() {
            return new LoanAccount(self());
        }
    }

    public abstract static class Builder<A extends LoanAccount, T extends LoanAccount.Builder<A, T>>
            extends Account.Builder<LoanAccount, T> {
        private Double interestRate;
        private LoanDetails details;

        public Builder(String uniqueIdentifier) {
            super(uniqueIdentifier);
        }

        public Double getInterestRate() {
            return this.interestRate;
        }

        public Builder<A, T> setInterestRate(Double interestRate) {
            this.interestRate = interestRate;
            return this;
        }

        @Deprecated
        @Override
        public Amount getBalance() {
            return new Amount(
                    super.getExactBalance().getCurrencyCode(),
                    ensureNegativeSign(super.getExactBalance()).getDoubleValue());
        }

        @Override
        public ExactCurrencyAmount getExactBalance() {
            return ensureNegativeSign(super.getExactBalance());
        }

        public LoanDetails getDetails() {
            return this.details != null
                    ? this.details
                    : LoanDetails.builder(LoanDetails.Type.DERIVE_FROM_NAME)
                            .setLoanNumber(getAccountNumber())
                            .build();
        }

        public Builder<A, T> setDetails(LoanDetails details) {
            this.details = details;
            return this;
        }
    }
}
