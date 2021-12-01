package se.tink.backend.aggregation.nxgen.core.account.transactional;

import se.tink.backend.agents.rpc.AccountTypes;

/**
 * @deprecated Use {@link TransactionalAccount#nxBuilder()} instead.
 *     <p>This will be removed as part of the improved step builder + agent builder refactoring
 *     project
 */
@Deprecated
public class CheckingAccount extends TransactionalAccount {

    /**
     * @deprecated Use {@link TransactionalAccount#nxBuilder()} instead.
     *     <p>This will be removed as part of the improved step builder + agent builder refactoring
     *     project
     */
    @Deprecated
    private CheckingAccount(Builder<CheckingAccount, DefaultCheckingAccountBuilder> builder) {
        super(builder);
    }

    /**
     * @deprecated Use {@link TransactionalAccount#nxBuilder()} instead.
     *     <p>This will be removed as part of the improved step builder + agent builder refactoring
     *     project
     */
    @Deprecated
    public static Builder<?, ?> builder(String uniqueIdentifier) {
        return new DefaultCheckingAccountBuilder(uniqueIdentifier);
    }

    @Override
    public AccountTypes getType() {
        return AccountTypes.CHECKING;
    }

    /**
     * @deprecated Use {@link TransactionalAccount#nxBuilder()} instead.
     *     <p>This will be removed as part of the improved step builder + agent builder refactoring
     *     project
     */
    @Deprecated
    public abstract static class Builder<
                    A extends CheckingAccount, T extends CheckingAccount.Builder<A, T>>
            extends TransactionalAccount.Builder<CheckingAccount, Builder<A, T>> {

        public Builder(String uniqueIdentifier) {
            super(uniqueIdentifier);
        }
    }

    /**
     * @deprecated Use {@link TransactionalAccount#nxBuilder()} instead.
     *     <p>This will be removed as part of the improved step builder + agent builder refactoring
     *     project
     */
    @Deprecated
    public static class DefaultCheckingAccountBuilder
            extends CheckingAccount.Builder<CheckingAccount, DefaultCheckingAccountBuilder> {

        public DefaultCheckingAccountBuilder(String uniqueIdentifier) {
            super(uniqueIdentifier);
        }

        @Deprecated
        @Override
        protected Builder self() {
            return this;
        }

        @Deprecated
        @Override
        public CheckingAccount build() {
            return new CheckingAccount(self());
        }
    }
}
