package se.tink.backend.insights.app.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Objects;
import se.tink.backend.insights.core.valueobjects.Amount;
import se.tink.backend.insights.core.valueobjects.UserId;


public class CreateLeftToSpendHighInsightCommand {

    private UserId userId;
    private Amount amount;

    public CreateLeftToSpendHighInsightCommand(UserId userId, Amount amount) {
        validate(userId);
        this.userId = userId;
        this.amount = amount;
    }

    public UserId getUserId() {
        return userId;
    }

    public Amount getAmount() {
        return amount;
    }

    private void validate(UserId userId) {
        Preconditions.checkArgument(!Objects.isNull(userId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId.value()));
    }
}
