package se.tink.backend.insights.app.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.insights.core.valueobjects.InsightId;
import se.tink.backend.insights.core.valueobjects.UserId;

public class ArchiveInsightCommand {

    private UserId userId;
    private InsightId insightId;

    public ArchiveInsightCommand(String userId, String insightId) {
        validate(userId, insightId);
        this.userId = UserId.of(userId);
        this.insightId = InsightId.of(insightId);
    }

    public InsightId getInsightId() {
        return insightId;
    }

    public UserId getUserId() {
        return userId;
    }

    private void validate(String userId, String insightId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(insightId));
    }
}
