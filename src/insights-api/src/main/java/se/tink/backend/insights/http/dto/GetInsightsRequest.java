package se.tink.backend.insights.http.dto;

public class GetInsightsRequest {
    private String userId;

    public GetInsightsRequest() {
    }

    public GetInsightsRequest(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
