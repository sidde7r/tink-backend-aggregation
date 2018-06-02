package se.tink.backend.insights.core.domain.model;

import com.google.common.collect.ImmutableList;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.insights.core.valueobjects.InsightAction;
import se.tink.backend.insights.core.valueobjects.InsightActionId;
import se.tink.backend.insights.core.valueobjects.InsightType;
import se.tink.backend.insights.core.valueobjects.TinkInsightScore;
import se.tink.backend.insights.core.valueobjects.InsightId;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.core.valueobjects.HtmlData;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.uuid.UUIDUtils;

public abstract class Insight implements InsightScore {

    private InsightId id;
    private UserId userId;
    private InsightType type;
    private InsightAction selectedAction;
    private byte[] data;
    private TinkInsightScore tinkInsightScore;
    private double relevanceScore;
    private Date created;
    private Date expirationDate;
    private Date startDate;
    private ImmutableList<InsightAction> actions;

    public Insight(UserId userId, InsightType type, TinkInsightScore tinkInsightScore) {
        this.id = InsightId.of(generateId());
        this.userId = userId;
        this.type = type;
        this.tinkInsightScore = tinkInsightScore;
        this.created = new Date();
    }

    private static String generateId() {
        return UUIDUtils.generateUUID();
    }

    public InsightId getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public InsightType getType() {
        return type;
    }

    public TinkInsightScore getTinkInsightScore() {
        return tinkInsightScore;
    }

    public InsightAction getSelectedAction() {
        return selectedAction;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public Date getCreated() {
        return created;
    }

    public abstract String composeTitle();

    public abstract String composeMessage();

    public abstract void calculateAndSetExpirationDate();

    public abstract void calculateAndSetStartDate();

    public abstract void generateAndSetInsightActions();

    public void setInsightActions(ImmutableList<InsightAction> actions) {
        this.actions = actions;
    }

    public ImmutableList<InsightAction> getActions() {
        return actions;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public boolean validPeriod(Date date) {
        return DateUtils.afterOrEqual(date, startDate) && DateUtils.beforeOrEqual(date, expirationDate);
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public void selectActionById(InsightActionId actionId) {
        this.selectedAction = actions.stream().filter(a -> a.getId().equals(actionId)).findFirst().get();
    }

    public boolean containsAction(InsightActionId actionId) {
        Optional<InsightAction> action = actions.stream().filter(a -> a.getId().equals(actionId)).findFirst();
        if (!action.isPresent()) {
            return false;
        }
        return true;
    }

    public abstract HtmlData getHtmlData();
}
