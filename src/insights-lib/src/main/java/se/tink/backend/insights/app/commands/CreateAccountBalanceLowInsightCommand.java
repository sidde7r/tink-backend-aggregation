package se.tink.backend.insights.app.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Objects;
import se.tink.backend.insights.core.valueobjects.Account;
import se.tink.backend.insights.core.valueobjects.UserId;

public class CreateAccountBalanceLowInsightCommand {

    private UserId userId;
    private List<Account> accounts;

    public CreateAccountBalanceLowInsightCommand(UserId userId,
            List<Account> accounts){
        validate(userId, accounts);
        this.userId = userId;
        this.accounts = accounts;
    }

    public UserId getUserId() {
        return userId;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    private void validate(UserId userId, List<Account> accounts){
        Preconditions.checkArgument(!Objects.isNull(userId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId.value()));
        Preconditions.checkArgument(accounts.size() > 0);
    }
}
