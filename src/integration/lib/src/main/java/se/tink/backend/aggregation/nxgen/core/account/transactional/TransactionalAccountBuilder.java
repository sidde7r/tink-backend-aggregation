package se.tink.backend.aggregation.nxgen.core.account.transactional;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import se.tink.backend.aggregation.nxgen.core.account.AccountBuilder;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.WithFlagPolicyStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithBalanceStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithIdStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.WithTypeStep;
import se.tink.libraries.account.enums.AccountFlag;

public class TransactionalAccountBuilder
        extends AccountBuilder<TransactionalAccount, TransactionalBuildStep>
        implements WithTypeStep<WithBalanceStep<TransactionalBuildStep>>,
                WithFlagPolicyStep<
                        WithBalanceStep<TransactionalBuildStep>, TransactionalAccountTypeMapper>,
                WithBalanceStep<TransactionalBuildStep>,
                TransactionalBuildStep {

    private BalanceModule balanceModule;
    private TransactionalAccountType accountType;

    @Override
    public WithFlagPolicyStep<
                    WithBalanceStep<TransactionalBuildStep>, TransactionalAccountTypeMapper>
            withType(@Nullable TransactionalAccountType accountType) {
        this.accountType = accountType;
        return this;
    }

    @Override
    public WithBalanceStep<TransactionalBuildStep> withTypeAndFlagsFrom(
            TransactionalAccountTypeMapper mapper,
            String typeKey,
            TransactionalAccountType defaultValue) {
        Preconditions.checkNotNull(mapper, "Mapper must not be null");

        accountType = mapper.translate(typeKey).orElse(defaultValue);
        accountFlags.addAll(mapper.getItems(typeKey));
        return this;
    }

    @Override
    public WithBalanceStep<TransactionalBuildStep> withTypeAndFlagsFrom(
            TransactionalAccountTypeMapper mapper, String typeKey) {
        withTypeAndFlagsFrom(mapper, typeKey, null);
        return this;
    }

    @Override
    public WithBalanceStep<TransactionalBuildStep> withTypeAndFlagsFrom(
            AccountTypeMapper mapper, String typeKey, TransactionalAccountType defaultValue) {
        Preconditions.checkNotNull(mapper, "Mapper must not be null");

        accountType =
                TransactionalAccountType.from(mapper.translate(typeKey).orElse(null))
                        .orElse(defaultValue);
        accountFlags.addAll(mapper.getItems(typeKey));
        return this;
    }

    @Override
    public WithBalanceStep<TransactionalBuildStep> withTypeAndFlagsFrom(
            AccountTypeMapper mapper, String typeKey) {
        withTypeAndFlagsFrom(mapper, typeKey, null);
        return this;
    }

    @Override
    public WithIdStep<TransactionalBuildStep> withBalance(@Nonnull BalanceModule balance) {
        Preconditions.checkNotNull(balance, "Balance Module must not be null.");
        this.balanceModule = balance;
        return this;
    }

    @Override
    public WithBalanceStep<TransactionalBuildStep> withFlagsFrom(
            TransactionalAccountTypeMapper mapper, String typeKey) {
        // If we have a mapper, we want to apply flags from that mapper
        accountFlags.addAll(mapper.getItems(typeKey));

        return this;
    }

    @Override
    public WithBalanceStep<TransactionalBuildStep> withInferredAccountFlags() {
        if (accountType == TransactionalAccountType.CHECKING) {
            accountFlags.add(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        }

        return this;
    }

    @Override
    public WithBalanceStep<TransactionalBuildStep> withPaymentAccountFlag() {
        accountFlags.add(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        return this;
    }

    @Override
    public WithBalanceStep<TransactionalBuildStep> withFlags(AccountFlag... flags) {
        this.accountFlags.addAll(Arrays.asList(flags));
        return this;
    }

    @Override
    public WithBalanceStep<TransactionalBuildStep> withoutFlags() {
        return this;
    }

    @Override
    protected TransactionalBuildStep buildStep() {
        return this;
    }

    @Override
    public Optional<TransactionalAccount> build() {
        return accountType == null
                ? Optional.empty()
                : Optional.of(new TransactionalAccount(this, balanceModule));
    }

    TransactionalAccountType getTransactionalType() {
        return accountType;
    }
}
