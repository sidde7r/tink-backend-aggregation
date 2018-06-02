package se.tink.backend.utils.guavaimpl.predicates;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import java.util.Collection;
import java.util.UUID;
import org.apache.commons.math3.util.Precision;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.AccountTypes;

public class AccountPredicate {
    public static final Predicate<Account> IS_NOT_EXCLUDED =
            account -> !account.isExcluded();

    public static final Predicate<Account> IS_NOT_CLOSED =
            account -> !account.isClosed();

    public static final Predicate<Account> IS_INCLUDED =
            account -> !account.isExcluded();

    public static final Predicate<Account> IS_FAVORED =
            Account::isFavored;

    public static final Predicate<Account> IS_LOAN = a -> Objects.equal(a.getType(), AccountTypes.LOAN);

    public static final Predicate<Account> IS_MORTGAGE = a -> Objects.equal(a.getType(), AccountTypes.MORTGAGE);

    public static final Predicate<Account> IS_CHECKING_ACCOUNT = a -> Objects.equal(a.getType(), AccountTypes.CHECKING);

    public static final Predicate<Account> IS_SAVINGS_ACCOUNT = a -> Objects.equal(a.getType(), AccountTypes.SAVINGS);

    public static final Predicate<Account> IS_CREDIT_CARD_ACCOUNT =
            account -> Objects.equal(AccountTypes.CREDIT_CARD, account.getType());

    public static final Predicate<Account> HAS_IDENTIFIER =
            account -> account.getIdentifiers() != null && account.getIdentifiers().size() > 0;

    public static final Predicate<? super Account> HAS_TRANSFER_DESTINATIONS =
            (Predicate<Account>) account ->
                    account.getTransferDestinations() != null && account.getTransferDestinations().size() > 0;

    /**
     * All ownership < 0.9 is considered shared because of floating point precision.
     */
    public static final Predicate<? super Account> IS_SHARED_ACCOUNT = (Predicate<Account>) account -> !Precision
            .equals(account.getOwnership(), 1.0, 0.1);

    public static Predicate<Account> accountBelongsToCredentials(final Collection<String> credentialsIds) {
        return account -> {
            if (credentialsIds == null) {
                return false;
            }

            return credentialsIds.contains(account.getCredentialsId());
        };
    }

    public static Predicate<Account> accountBelongsToCredential(UUID credentialsId) {
        final String credentialsIdString = UUIDUtils.toTinkUUID(credentialsId);

        return account -> account != null && Objects.equal(account.getCredentialsId(), credentialsIdString);
    }

    public static final Predicate<Account> HAS_SWEDISH_ACCOUNT_IDENTIFIER =
            account -> {
                if (account == null) {
                    return false;
                }

                if (account.getIdentifiers() == null || account.getIdentifiers().isEmpty()) {
                    return false;
                }

                for (AccountIdentifier identifier : account.getIdentifiers()) {
                    if (identifier != null && identifier.is(AccountIdentifier.Type.SE)) {
                        return true;
                    }
                }

                return false;
            };

    public static Predicate<? super Account> balanceGreaterThan(final double balance) {
        return (Predicate<Account>) account -> account.getBalance() > balance;
    }
}
