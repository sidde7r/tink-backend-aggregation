package se.tink.backend.insights.accounts;

import java.util.List;
import se.tink.backend.insights.core.valueobjects.Account;
import se.tink.backend.insights.core.valueobjects.UserId;

public interface AccountQueryService {
    List<Account> getCheckingAccounts(UserId userId);
}
