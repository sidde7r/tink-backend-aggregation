package se.tink.backend.insights.http.dto;

import java.util.List;

public class GetRenderedResponse {
    private List<String> html;

    public GetRenderedResponse(List<String> html) {
        this.html = html;
    }

    public GetRenderedResponse() {
    }

    public List<String> getHtml() {
        return html;
    }

    public void setHtml(List<String> html) {
        this.html = html;
    }
}
