package se.tink.backend.rpc;

import io.protostuff.Tag;

public class FraudDetailsHelpResponse {
    @Tag(1)
    private String html;

    public FraudDetailsHelpResponse(String html) {
        this.html = html;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
