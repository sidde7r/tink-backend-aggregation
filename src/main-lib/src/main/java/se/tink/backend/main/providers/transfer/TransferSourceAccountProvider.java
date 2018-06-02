package se.tink.backend.main.providers.transfer;

import java.util.Optional;
import java.util.List;
import java.util.Set;
import se.tink.backend.core.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.User;

public interface TransferSourceAccountProvider {
    List<Account> getSourceAccountsAll(User user);
    List<Account> getSourceAccountsExplicitTypes(User user, Set<AccountIdentifier.Type> explicitTypes);

    List<Account> getSourceAccounts(User user,
            Optional<? extends Set<AccountIdentifier.Type>> explicitTypes,
            Optional<? extends Set<AccountIdentifier>> explicitIdentifiers);
}
