package se.tink.backend.api;

import java.util.List;
import se.tink.backend.core.Account;
import se.tink.backend.core.User;
import se.tink.backend.rpc.AccountListResponse;

public interface AccountService {

    @Deprecated
    List<Account> list(User user);

    AccountListResponse listAccounts(User user);

    Account update(User user, String id, Account account);
}
