package se.tink.backend.aggregation.nxgen.core.account.transactional;

import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.backend.agents.rpc.AccountTypes;

/**
 * This acts as a subset of {@link AccountTypes} to enforce compile-time checks on transactional
 * account types.
 */
public enum TransactionalAccountType {
    CHECKING,
    SAVINGS,
    @Deprecated
    OTHER;

    public static Optional<TransactionalAccountType> from(@Nullable AccountTypes type) {
        if (type == null) {
            return Optional.empty();
        }

        switch (type) {
            case CHECKING:
                return Optional.of(CHECKING);
            case SAVINGS:
                return Optional.of(SAVINGS);
            case OTHER:
                return Optional.of(OTHER);
            default:
                return Optional.empty();
        }
    }

    public AccountTypes toAccountType() {
        switch (this) {
            case CHECKING:
                return AccountTypes.CHECKING;
            case SAVINGS:
                return AccountTypes.SAVINGS;
            default:
            case OTHER:
                return AccountTypes.OTHER;
        }
    }

    public static boolean isTransactionalAccount(AccountTypes type) {
        Optional<TransactionalAccountType> transactionalAccountType =
                TransactionalAccountType.from(type);
        return transactionalAccountType.isPresent()
                && transactionalAccountType.orElse(null) != OTHER;
    }

    public static boolean isNotTransactionalAccount(AccountTypes type) {
        return !isTransactionalAccount(type);
    }
}
