package se.tink.backend.aggregation.nxgen.core.account.transactional;

import javax.annotation.Nonnull;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.AccountIdentifierStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.AccountNumberStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.AliasStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.BalanceStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.SavingsBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.UniqueIdentifierStep;
import se.tink.libraries.amount.Amount;

@Deprecated
public class SavingsAccount extends TransactionalAccount {
    private final Double interestRate;

    @Deprecated
    private SavingsAccount(Builder<?, ?> builder) {
        super(builder);
        this.interestRate = builder.getInterestRate();
    }

    private SavingsAccount(SavingsAccountBuilder builder) {
        super(builder);
        this.interestRate = builder.getInterestRate();
    }

    /** @deprecated Use SavingsAccount.builder() instead */
    @Deprecated
    public static Builder<?, ?> builder(String uniqueIdentifier) {
        return new DefaultSavingAccountsBuilder(uniqueIdentifier);
    }

    /** @deprecated Use SavingsAccount.builder() instead */
    @Deprecated
    public static Builder<?, ?> builder(String uniqueIdentifier, Amount balance) {
        return builder(uniqueIdentifier).setBalance(balance);
    }

    public static UniqueIdentifierStep<SavingsBuildStep> builder() {
        return new SavingsAccountBuilder();
    }

    @Override
    public AccountTypes getType() {
        return AccountTypes.SAVINGS;
    }

    public Double getInterestRate() {
        return this.interestRate;
    }

    private static class SavingsAccountBuilder
            extends Account.StepBuilder<SavingsAccount, SavingsBuildStep>
            implements UniqueIdentifierStep<SavingsBuildStep>,
                    AccountNumberStep<SavingsBuildStep>,
                    BalanceStep<SavingsBuildStep>,
                    AliasStep<SavingsBuildStep>,
                    AccountIdentifierStep<SavingsBuildStep>,
                    SavingsBuildStep {

        private Double interestRate;

        @Override
        public AccountNumberStep<SavingsBuildStep> setUniqueIdentifier(
                @Nonnull String uniqueIdentifier) {
            applyUniqueIdentifier(uniqueIdentifier);
            return this;
        }

        @Override
        public BalanceStep<SavingsBuildStep> setAccountNumber(@Nonnull String accountNumber) {
            applyAccountNumber(accountNumber);
            return this;
        }

        @Override
        public AliasStep<SavingsBuildStep> setBalance(@Nonnull Amount balance) {
            applyBalance(balance);
            return this;
        }

        @Override
        public AccountIdentifierStep<SavingsBuildStep> setAlias(@Nonnull String alias) {
            applyAlias(alias);
            return this;
        }

        @Override
        public SavingsBuildStep setInterestRate(@Nonnull Double interestRate) {
            this.interestRate = interestRate;
            return this;
        }

        @Override
        public SavingsAccount build() {
            return new SavingsAccount(this);
        }

        Double getInterestRate() {
            return interestRate;
        }

        @Override
        protected SavingsBuildStep buildStep() {
            return this;
        }
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
