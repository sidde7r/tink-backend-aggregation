package se.tink.backend.aggregation.nxgen.core.account.transactional;

import se.tink.backend.agents.rpc.AccountTypes;

@Deprecated
public class SavingsAccount extends TransactionalAccount {
    private final Double interestRate;

    @Deprecated
    private SavingsAccount(Builder<?, ?> builder) {
        super(builder);
        this.interestRate = builder.getInterestRate();
    }

    /** @deprecated Use SavingsAccount.builder() instead */
    @Deprecated
    public static Builder<?, ?> builder(String uniqueIdentifier) {
        return new DefaultSavingAccountsBuilder(uniqueIdentifier);
    }

    @Override
    public AccountTypes getType() {
        return AccountTypes.SAVINGS;
    }

    public Double getInterestRate() {
        return this.interestRate;
    }

    /** @deprecated Use SavingsAccountBuilder instead */
    @Deprecated
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

    /** @deprecated Use SavingsAccountBuilder instead */
    @Deprecated
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
