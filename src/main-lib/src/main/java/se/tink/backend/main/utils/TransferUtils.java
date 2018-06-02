package se.tink.backend.main.utils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.Optional;
import se.tink.backend.common.repository.cassandra.SignableOperationRepository;
import se.tink.backend.core.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.User;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.enums.SignableOperationTypes;
import se.tink.backend.core.signableoperation.SignableOperation;

public class TransferUtils {
    public static boolean matches(TransferDestinationPattern pattern, AccountIdentifier identifier) {
        if (identifier == null) {
            return false;
        }
        if (pattern.getType() != identifier.getType()) {
            return false;
        }
        if (!identifier.isValid()) {
            return false;
        }

        Pattern p = Pattern.compile(pattern.getPattern());
        return p.matcher(identifier.getIdentifier()).matches();
    }

    public static boolean matchesAny(List<TransferDestinationPattern> patterns, AccountIdentifier identifier) {
        if (patterns == null || patterns.isEmpty() || identifier == null) {
            return false;
        }

        for (TransferDestinationPattern pattern : patterns) {
            if (TransferUtils.matches(pattern, identifier)) {
                return true;
            }
        }
        return false;
    }

    public static Optional<AccountIdentifier> findFirstMatch(List<TransferDestinationPattern> patterns, List<AccountIdentifier> identifiers) {
        if (identifiers == null || identifiers.isEmpty()) {
            return Optional.empty();
        }

        for (AccountIdentifier identifier : identifiers) {
            if (matchesAny(patterns, identifier)) {
                return Optional.of(identifier);
            }
        }

        return Optional.empty();
    }

    public static Optional<Account> findAccountDefinedByIdentifier(List<Account> accounts, AccountIdentifier identifier) {
        if (accounts == null || accounts.isEmpty() || identifier == null || !identifier.isValid()) {
            return Optional.empty();
        }

        for (Account account : accounts) {
            if (account.definedBy(identifier)) {
                return Optional.of(account);
            }
        }

        return Optional.empty();
    }

    // TODO - Why don't we use this one?
    public static boolean hasTransferInProgress(SignableOperationRepository repository, User user) {
        List<SignableOperation> signableOperations = repository
                .findAllByUserIdAndType(user.getId(), SignableOperationTypes.TRANSFER);

        for (SignableOperation o : signableOperations) {
            if (o.isInProgress()) {
                return true;
            }
        }

        return false;
    }
}
