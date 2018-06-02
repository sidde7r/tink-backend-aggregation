package se.tink.backend.insights.http.dto;

public class SelectActionRequest {
    private String userId;
    private String insightId;
    private String actionId;

    public SelectActionRequest() {
    }

    public SelectActionRequest(String userId, String insightId, String actionId) {
        this.userId = userId;
        this.insightId = insightId;
        this.actionId = actionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getInsightId() {
        return insightId;
    }

    public void setInsightId(String insightId) {
        this.insightId = insightId;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }
}
