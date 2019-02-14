package se.tink.backend.aggregation.nxgen.core.account.transactional;

import com.google.common.collect.ImmutableList;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.amount.Amount;

public abstract class TransactionalAccount extends Account {
    public static final ImmutableList<AccountTypes> ALLOWED_ACCOUNT_TYPES =
            ImmutableList.<AccountTypes>builder()
                    .add(AccountTypes.SAVINGS)
                    .add(AccountTypes.CHECKING)
                    .add(AccountTypes.OTHER)
                    .build();

    @Deprecated
    protected TransactionalAccount(Builder<?, ?> builder) {
        super(builder);
    }

    protected TransactionalAccount(StepBuilder<? extends TransactionalAccount, ?> builder) {
        super(builder);
    }

    /** @deprecated Use CheckingAccount.builder() or SavingsAccount.builder() instead */
    @Deprecated
    public static Builder<?, ?> builder(
            AccountTypes type, String uniqueIdentifier, Amount balance) {
        return builder(type, uniqueIdentifier).setBalance(balance);
    }

    /** @deprecated Use CheckingAccount.builder() or SavingsAccount.builder() instead */
    @Deprecated
    public static Builder<? extends Account, ?> builder(
            AccountTypes type, String uniqueIdentifier) {
        switch (type) {
            case SAVINGS:
                return SavingsAccount.builder(uniqueIdentifier);
            case CHECKING:
                return CheckingAccount.builder(uniqueIdentifier);
            case OTHER:
                return OtherAccount.builder(uniqueIdentifier);
            default:
                throw new IllegalStateException(
                        String.format("Unknown TransactionalAccount type (%s)", type));
        }
    }

    /** @deprecated Use StepBuilder instead */
    @Deprecated
    public abstract static class Builder<A extends TransactionalAccount, T extends Builder<A, T>>
            extends Account.Builder<A, T> {

        public Builder(String uniqueIdentifier) {
            super(uniqueIdentifier);
        }

        @Override
        protected abstract T self();

        @Override
        public abstract A build();
    }
}
