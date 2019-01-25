package se.tink.backend.aggregation.nxgen.core.account;

import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;

public class SavingsAccount extends TransactionalAccount {
    private final Double interestRate;

    private SavingsAccount(Builder<?, ?> builder) {
        super(builder);
        this.interestRate = builder.getInterestRate();
    }

    public static Builder<?, ?> builder(String uniqueIdentifier) {
        return new DefaultSavingAccountsBuilder(uniqueIdentifier);
    }

    public static Builder<?, ?> builder(String uniqueIdentifier, Amount balance) {
        return builder(uniqueIdentifier)
                .setBalance(balance);
    }

    @Override
    public AccountTypes getType() {
        return AccountTypes.SAVINGS;
    }

    public Double getInterestRate() {
        return this.interestRate;
    }

    public abstract static class Builder<
            A extends SavingsAccount, T extends SavingsAccount.Builder<A, T>>
            extends TransactionalAccount.Builder<SavingsAccount, Builder<A, T>> {
        private Double interestRate;

        public Builder(String uniqueIdentifier) {
            super(uniqueIdentifier);
        }

        public Double getInterestRate() {
            return this.interestRate;
        }

        public Builder<A, T> setInterestRate(Double interestRate) {
            self().interestRate = interestRate;
            return self();
        }
    }

    private static class DefaultSavingAccountsBuilder
            extends SavingsAccount.Builder<SavingsAccount, DefaultSavingAccountsBuilder> {

        public DefaultSavingAccountsBuilder(String uniqueIdentifier) {
            super(uniqueIdentifier);
        }

        @Override
        protected DefaultSavingAccountsBuilder self() {
            return this;
        }

        @Override
        public SavingsAccount build() {
            return new SavingsAccount(this);
        }
    }
}
