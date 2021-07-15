package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional;

import javax.annotation.Nullable;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.WithFlagPolicyStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithBalanceStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public interface WithTypeStep<T> {

    /**
     * Set the type for this transactional account. Setting a null type will result in this
     * <strong>entire builder returning null.</strong>
     *
     * @param accountType Nullable account type
     */
    WithFlagPolicyStep<T, TransactionalAccountTypeMapper> withType(
            @Nullable TransactionalAccountType accountType);

    /**
     * Infer both the transactional account type and account flags from the provided type mapper,
     * using the provided type key. Failure to map type using the mapper will result in a
     * transactional account of type {@code defaultValue}.
     */
    T withTypeAndFlagsFrom(
            AccountTypeMapper mapper, String typeKey, TransactionalAccountType defaultValue);

    WithBalanceStep<TransactionalBuildStep> withPatternTypeAndFlagsFrom(
            AccountTypeMapper mapper, String typeKey, TransactionalAccountType defaultValue);

    /**
     * Infer both the transactional account type and account flags from the provided type mapper,
     * using the provided type key. Failure to map type using the mapper will result in this
     * <strong>entire builder returning null.</strong>
     */
    T withTypeAndFlagsFrom(AccountTypeMapper mapper, String typeKey);

    T withTypeAndFlagsFrom(TransactionalAccountTypeMapper mapper, String typeKey);

    T withTypeAndFlagsFrom(
            TransactionalAccountTypeMapper mapper,
            String typeKey,
            TransactionalAccountType defaultValue);
}
