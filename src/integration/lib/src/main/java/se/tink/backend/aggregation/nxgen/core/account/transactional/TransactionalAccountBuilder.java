package se.tink.backend.aggregation.nxgen.core.account.transactional;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.AccountBuilder;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithIdStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.WithTypeStep;

public class TransactionalAccountBuilder
        extends AccountBuilder<TransactionalAccount, TransactionalBuildStep>
        implements WithTypeStep<TransactionalBuildStep>, TransactionalBuildStep {

    private static final ImmutableList<AccountTypes> ALLOWED_ACCOUNT_TYPES =
            ImmutableList.<AccountTypes>builder()
                    .add(AccountTypes.SAVINGS)
                    .add(AccountTypes.CHECKING)
                    .add(AccountTypes.OTHER)
                    .build();

    private AccountTypes accountType;

    @Override
    public WithIdStep<TransactionalBuildStep> withType(AccountTypes accountType) {
        Preconditions.checkArgument(
                ALLOWED_ACCOUNT_TYPES.contains(accountType),
                "Account Type must be CHECKING, SAVINGS or OTHER.");
        this.accountType = accountType;
        return this;
    }

    @Override
    protected TransactionalBuildStep buildStep() {
        return this;
    }

    @Override
    public TransactionalAccount build() {
        return new TransactionalAccount(this);
    }

    public AccountTypes getAccountType() {
        return accountType;
    }
}
