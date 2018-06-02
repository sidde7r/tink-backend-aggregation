package se.tink.backend.insights.app.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.insights.core.valueobjects.UserId;

public class RemovePreviousInsightsCommand {
    private UserId userId;

    public RemovePreviousInsightsCommand(String userId) {
        validate(userId);
        this.userId = UserId.of(userId);

    }

    public UserId getUserId() {
        return userId;
    }

    private void validate(String userId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId));
    }
}
