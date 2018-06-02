package se.tink.backend.insights.http.dto;

public class SelectActionResponse {
    private String message;

    public SelectActionResponse() {
    }

    public SelectActionResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
