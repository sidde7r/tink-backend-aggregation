package se.tink.backend.insights.app.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.insights.core.valueobjects.InsightActionId;
import se.tink.backend.insights.core.valueobjects.InsightId;
import se.tink.backend.insights.core.valueobjects.UserId;

public class SetInsightChoiceCommand {

    private UserId userId;
    private InsightId insightId;
    private InsightActionId actionId;

    public SetInsightChoiceCommand(String userId, String insightId, String actionId) {
        validate(userId, insightId, actionId);
        this.userId = UserId.of(userId);
        this.insightId = InsightId.of(insightId);
        this.actionId = InsightActionId.of(actionId);
    }

    public InsightId getInsightId() {
        return insightId;
    }

    public UserId getUserId() {
        return userId;
    }

    public InsightActionId getActionId() {
        return actionId;
    }

    private void validate(String userId, String insightId, String actionId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(insightId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(actionId));
    }
}
