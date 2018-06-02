package se.tink.backend.rpc;

import io.protostuff.Tag;

public class HtmlDetailsResponse {
    @Tag(1)
    private String buttonLabel;
    @Tag(2)
    private String html;
    @Tag(3)
    private String shareableMessage;

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getShareableMessage() {
        return shareableMessage;
    }

    public void setShareableMessage(String shareableMessage) {
        this.shareableMessage = shareableMessage;
    }

    public String getButtonLabel() {
        return buttonLabel;
    }

    public void setButtonLabel(String buttonLabel) {
        this.buttonLabel = buttonLabel;
    }
}
