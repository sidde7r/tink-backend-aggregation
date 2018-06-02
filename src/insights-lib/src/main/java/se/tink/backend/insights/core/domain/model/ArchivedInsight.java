package se.tink.backend.insights.core.domain.model;

import java.util.Date;
import se.tink.backend.insights.core.valueobjects.InsightAction;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.core.valueobjects.InsightType;
import se.tink.backend.insights.core.valueobjects.InsightId;

public class ArchivedInsight {

    private InsightId id;
    private UserId userId;
    private InsightType insightType;
    private InsightAction action;
    private byte[] data;
    private String message;
    private Date dateInsightCreated;
    private Date dateArchived;

    public ArchivedInsight(InsightId id, UserId userId, InsightType insightType, InsightAction action, String message, Date dateInsightCreated) {
        this.id = id;
        this.userId = userId;
        this.insightType = insightType;
        this.action = action;
        this.message = message;
        this.dateInsightCreated = dateInsightCreated;
        this.dateArchived = new Date();
    }

    public InsightId getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public InsightType getInsightType() {
        return insightType;
    }

    public InsightAction getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }

    public Date getDateInsightCreated() {
        return dateInsightCreated;
    }

    public Date getDateArchived() {
        return dateArchived;
    }
}
