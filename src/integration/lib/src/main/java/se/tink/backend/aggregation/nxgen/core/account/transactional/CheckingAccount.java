package se.tink.backend.aggregation.nxgen.core.account.transactional;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.AccountIdentifierStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.AccountNumberStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.AliasStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.BalanceStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.CheckingBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.UniqueIdentifierStep;
import se.tink.libraries.amount.Amount;

import javax.annotation.Nonnull;

public class CheckingAccount extends TransactionalAccount {

    @Deprecated
    private CheckingAccount(Builder<CheckingAccount, DefaultCheckingAccountBuilder> builder) {
        super(builder);
    }

    private CheckingAccount(CheckingAccountBuilder builder) {
        super(builder);
    }

    public static Builder<?, ?> builder(String uniqueIdentifier) {
        return new DefaultCheckingAccountBuilder(uniqueIdentifier);
    }

    public static Builder<?, ?> builder(String uniqueIdentifier, Amount balance) {
        return builder(uniqueIdentifier).setBalance(balance);
    }

    public static UniqueIdentifierStep<CheckingBuildStep> builder() {
        return new CheckingAccountBuilder();
    }

    private static class CheckingAccountBuilder
            extends Account.StepBuilder<CheckingAccount, CheckingBuildStep>
            implements UniqueIdentifierStep<CheckingBuildStep>,
                    AccountNumberStep<CheckingBuildStep>,
                    BalanceStep<CheckingBuildStep>,
                    AliasStep<CheckingBuildStep>,
                    AccountIdentifierStep<CheckingBuildStep>,
                    CheckingBuildStep {

        @Override
        public AccountNumberStep<CheckingBuildStep> setUniqueIdentifier(
                @Nonnull String uniqueIdentifier) {
            applyUniqueIdentifier(uniqueIdentifier);
            return this;
        }

        @Override
        public BalanceStep<CheckingBuildStep> setAccountNumber(@Nonnull String accountNumber) {
            applyAccountNumber(accountNumber);
            return this;
        }

        @Override
        public AliasStep<CheckingBuildStep> setBalance(@Nonnull Amount balance) {
            applyBalance(balance);
            return this;
        }

        @Override
        public AccountIdentifierStep<CheckingBuildStep> setAlias(String alias) {
            applyAlias(alias);
            return this;
        }

        @Override
        public CheckingAccount build() {
            return new CheckingAccount(this);
        }

        @Override
        protected CheckingBuildStep buildStep() {
            return this;
        }
    }

    @Override
    public AccountTypes getType() {
        return AccountTypes.CHECKING;
    }

    /** @deprecated Use CheckingAccountBuilder instead */
    @Deprecated
    public abstract static class Builder<
                    A extends CheckingAccount, T extends CheckingAccount.Builder<A, T>>
            extends TransactionalAccount.Builder<CheckingAccount, Builder<A, T>> {

        public Builder(String uniqueIdentifier) {
            super(uniqueIdentifier);
        }
    }

    /** @deprecated Use CheckingAccountBuilder instead */
    @Deprecated
    public static class DefaultCheckingAccountBuilder
            extends CheckingAccount.Builder<CheckingAccount, DefaultCheckingAccountBuilder> {

        public DefaultCheckingAccountBuilder(String uniqueIdentifier) {
            super(uniqueIdentifier);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public CheckingAccount build() {
            return new CheckingAccount(self());
        }
    }
}
