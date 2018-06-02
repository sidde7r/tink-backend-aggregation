package se.tink.backend.insights.app.commands;

import com.google.common.base.Preconditions;
import java.util.Objects;
import se.tink.backend.insights.core.valueobjects.UserId;

public class CreateAllBanksConnectedCommand {
    private UserId userId;

    public CreateAllBanksConnectedCommand(UserId userId) {
        validate(userId);
        this.userId = userId;
    }

    public UserId getUserId() {
        return userId;
    }

    private void validate(UserId userId) {
        Preconditions.checkArgument(!Objects.isNull(userId));
    }
}
