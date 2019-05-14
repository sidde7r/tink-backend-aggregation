package se.tink.backend.aggregation.nxgen.core.account.transactional;

import se.tink.backend.agents.rpc.AccountTypes;

/**
 * This acts as a subset of {@link AccountTypes} to enforce compile-time checks on transactional
 * account types.
 */
public enum TransactionalAccountType {
    CHECKING,
    SAVINGS,
    OTHER;

    public static TransactionalAccountType from(AccountTypes type) {
        switch (type) {
            case CHECKING:
                return CHECKING;
            case SAVINGS:
                return SAVINGS;
            case OTHER:
                return OTHER;
            default:
                throw new IllegalArgumentException(
                        "Account Type must be CHECKING, SAVINGS or OTHER.");
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
}
