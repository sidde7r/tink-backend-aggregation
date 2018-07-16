package se.tink.backend.aggregation.nxgen.core.account;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;

public class LoanAccount extends Account {
    private final Double interestRate;
    private final LoanDetails details;

    private LoanAccount(Builder<LoanAccount, DefaultLoanBuilder> builder) {
        super(builder);
        this.interestRate = builder.getInterestRate();
        this.details = builder.getDetails();
    }

    public static Builder<?, ?> builder() {
        return new DefaultLoanBuilder();
    }

    public static Builder<?, ?> builder(String uniqueIdentifier, Amount balance) {
        DefaultLoanBuilder defaultLoanBuilder = new DefaultLoanBuilder();
        defaultLoanBuilder
                .setUniqueIdentifier(uniqueIdentifier)
                .setBalance(balance);
        return defaultLoanBuilder;
    }

    private static Amount ensureNegativeSign(Amount amount) {
        Preconditions.checkNotNull(amount);
        Preconditions.checkNotNull(amount.getValue());
        Preconditions.checkArgument(amount.getValue() <= 0);
        return amount;
    }

    @Override
    public AccountTypes getType() {
        return AccountTypes.LOAN;
    }

    @Override
    public Amount getBalance() {
        return Amount.createFromAmount(super.getBalance()).orElseThrow(NullPointerException::new);
    }

    public Double getInterestRate() {
        return this.interestRate;
    }

    public LoanDetails getDetails() {
        return this.details;
    }

    private static class DefaultLoanBuilder
            extends LoanAccount.Builder<LoanAccount, DefaultLoanBuilder> {

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

        public Double getInterestRate() {
            return this.interestRate;
        }

        public Builder<A, T> setInterestRate(Double interestRate) {
            this.interestRate = interestRate;
            return this;
        }

        @Override
        public Amount getBalance() {
            return ensureNegativeSign(super.getBalance());
        }

        public LoanDetails getDetails() {
            return this.details != null
                    ? this.details
                    : LoanDetails.builder().setName(getName()).setLoanNumber(getAccountNumber()).build();
        }

        public Builder<A, T> setDetails(LoanDetails details) {
            this.details = details;
            return this;
        }
    }
}
