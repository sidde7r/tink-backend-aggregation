package se.tink.backend.aggregation.nxgen.core.account.transactional;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.nxgen.core.account.AccountBuilder;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithBalanceStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithIdStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.WithTypeStep;

public class TransactionalAccountBuilder
        extends AccountBuilder<TransactionalAccount, TransactionalBuildStep>
        implements WithTypeStep<WithBalanceStep<WithIdStep<TransactionalBuildStep>>>,
                WithBalanceStep<WithIdStep<TransactionalBuildStep>>,
                TransactionalBuildStep {

    private BalanceModule balanceModule;
    private TransactionalAccountType accountType;

    @Override
    public WithBalanceStep<WithIdStep<TransactionalBuildStep>> withType(
            @Nonnull TransactionalAccountType accountType) {
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
        return new TransactionalAccount(this, balanceModule);
    }

    TransactionalAccountType getTransactionalType() {
        return accountType;
    }

    @Override
    public WithIdStep<TransactionalBuildStep> withBalance(@Nonnull BalanceModule balance) {
        Preconditions.checkNotNull(balance, "Balance Module must not be null.");
        this.balanceModule = balance;
        return this;
    }
}
