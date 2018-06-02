package se.tink.backend.insights.app.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Objects;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.libraries.identity.model.IdentityEvent;

public class CreateGenericFraudWarningInsightCommand {

    private UserId userId;
    private List<IdentityEvent> identityEvents;

    public CreateGenericFraudWarningInsightCommand(UserId userId, List<IdentityEvent> identityEvents) {
        validate(userId, identityEvents);
        this.userId = userId;
        this.identityEvents = identityEvents;

    }

    public UserId getUserId() {
        return userId;
    }

    public List<IdentityEvent> getIdentityEvents() {
        return identityEvents;
    }

    private void validate(UserId userId, List<IdentityEvent> identityEvents) {
        Preconditions.checkArgument(!Objects.isNull(userId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId.value()));
        Preconditions.checkArgument(!Objects.isNull(identityEvents));
        Preconditions.checkArgument(identityEvents.size() > 0);
    }
}
