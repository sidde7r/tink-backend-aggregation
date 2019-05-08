package se.tink.backend.aggregation.nxgen.core.account.transactional;

import com.google.common.collect.ImmutableList;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.WithTypeStep;
import se.tink.libraries.amount.Amount;

public class TransactionalAccount extends Account {
    public static final ImmutableList<AccountTypes> ALLOWED_ACCOUNT_TYPES =
            ImmutableList.<AccountTypes>builder()
                    .add(AccountTypes.SAVINGS)
                    .add(AccountTypes.CHECKING)
                    .add(AccountTypes.OTHER)
                    .build();

    private AccountTypes accountType;

    TransactionalAccount(TransactionalAccountBuilder builder) {
        super(builder);
        this.accountType = builder.getAccountType();
    }

    public static WithTypeStep<TransactionalBuildStep> nxBuilder() {
        return new TransactionalAccountBuilder();
    }

    // This will be removed as part of the improved step builder + agent builder refactoring project
    @Deprecated
    protected TransactionalAccount(Builder<?, ?> builder) {
        super(builder);
    }

    protected TransactionalAccount(StepBuilder<? extends TransactionalAccount, ?> builder) {
        super(builder);
    }

    // This will be removed as part of the improved step builder + agent builder refactoring project
    /** @deprecated Use CheckingAccount.builder() or SavingsAccount.builder() instead */
    @Deprecated
    public static Builder<?, ?> builder(
            AccountTypes type, String uniqueIdentifier, Amount balance) {
        return builder(type, uniqueIdentifier).setBalance(balance);
    }

    // This will be removed as part of the improved step builder + agent builder refactoring project
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

    @Override
    public AccountTypes getType() {
        return accountType;
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
