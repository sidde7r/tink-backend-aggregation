package se.tink.backend.common.workers.activity.generators.models;

public class ShareableDetailsHtmlActivityData extends HtmlActivityData {
    private String buttonLabel;
    private String detailsHtml;
    private String shareableMessage;

    public String getButtonLabel() {
        return buttonLabel;
    }

    public String getDetailsHtml() {
        return detailsHtml;
    }

    public String getShareableMessage() {
        return shareableMessage;
    }

    public void setButtonLabel(String buttonLabel) {
        this.buttonLabel = buttonLabel;
    }

    public void setDetailsHtml(String detailsHtml) {
        this.detailsHtml = detailsHtml;
    }

    public void setShareableMessage(String shareableMessage) {
        this.shareableMessage = shareableMessage;
    }
}
