package se.tink.backend.aggregation.nxgen.core.account.transactional;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.nxgen.core.account.AccountBuilder;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithIdStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.WithTypeStep;

public class TransactionalAccountBuilder
        extends AccountBuilder<TransactionalAccount, TransactionalBuildStep>
        implements WithTypeStep<TransactionalBuildStep>, TransactionalBuildStep {

    private TransactionalAccountType accountType;

    @Override
    public WithIdStep<TransactionalBuildStep> withType(TransactionalAccountType accountType) {
        Preconditions.checkNotNull(accountType, "Account Type must not be null.");
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

    public TransactionalAccountType getAccountType() {
        return accountType;
    }
}
