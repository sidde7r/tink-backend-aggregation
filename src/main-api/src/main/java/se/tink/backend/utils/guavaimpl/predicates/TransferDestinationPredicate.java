package se.tink.backend.utils.guavaimpl.predicates;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.transfer.TransferDestination;

public class TransferDestinationPredicate {
    public static final Predicate<TransferDestination> IS_BG_OR_PG_DESTINATION =
            transferDestination -> {
                AccountIdentifier identifier = AccountIdentifier.create(transferDestination.getUri());
                AccountIdentifier.Type type = identifier.getType();

                return Objects.equal(type, AccountIdentifier.Type.SE_BG) ||
                        Objects.equal(type, AccountIdentifier.Type.SE_PG);
            };
}
