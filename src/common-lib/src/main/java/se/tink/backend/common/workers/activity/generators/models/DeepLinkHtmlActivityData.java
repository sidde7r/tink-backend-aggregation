package se.tink.backend.common.workers.activity.generators.models;

public class DeepLinkHtmlActivityData extends HtmlActivityData {
    private String buttonLabel;
    private String deepLink;

    public String getButtonLabel() {
        return buttonLabel;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public void setButtonLabel(String buttonLabel) {
        this.buttonLabel = buttonLabel;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }
}
