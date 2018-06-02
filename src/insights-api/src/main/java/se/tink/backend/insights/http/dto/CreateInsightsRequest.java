package se.tink.backend.insights.http.dto;

public class CreateInsightsRequest {
    private String userId;

    public CreateInsightsRequest() {
    }

    public CreateInsightsRequest(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
