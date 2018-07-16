package se.tink.backend.aggregation.nxgen.core.account;

import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;

public class CheckingAccount extends TransactionalAccount {

    private CheckingAccount(Builder<CheckingAccount, DefaultCheckingAccountBuilder> builder) {
        super(builder);
    }

    public static Builder<?, ?> builder(String uniqueIdentifier) {
        return new DefaultCheckingAccountBuilder()
                .setUniqueIdentifier(uniqueIdentifier);
    }

    public static Builder<?, ?> builder(String uniqueIdentifier, Amount balance) {
        return builder(uniqueIdentifier)
                .setBalance(balance);
    }

    @Override
    public AccountTypes getType() {
        return AccountTypes.CHECKING;
    }

    public abstract static class Builder<A extends CheckingAccount, T extends CheckingAccount.Builder<A, T>>
            extends TransactionalAccount.Builder<CheckingAccount, Builder<A, T>> {
    }

    public static class DefaultCheckingAccountBuilder
            extends CheckingAccount.Builder<CheckingAccount, DefaultCheckingAccountBuilder> {

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
