package se.tink.backend.insights.app.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Objects;
import se.tink.backend.insights.core.valueobjects.UserId;

public class CreateRateAppInsightCommand {

    private UserId userId;

    public CreateRateAppInsightCommand(UserId userId) {
        validate(userId);
        this.userId = userId;
    }

    public UserId getUserId() {
        return userId;
    }

    private void validate(UserId userId) {
        Preconditions.checkArgument(!Objects.isNull(userId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId.value()));
    }
}
