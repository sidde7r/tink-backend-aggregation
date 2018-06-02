package se.tink.backend.insights.http.dto;

public class GetRenderedRequest {
    private String userId;
    private int offset;
    private int limit;

    public GetRenderedRequest() {
    }

    public GetRenderedRequest(String userId, int offset, int limit) {
        this.userId = userId;
        this.offset = offset;
        this.limit = limit;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
