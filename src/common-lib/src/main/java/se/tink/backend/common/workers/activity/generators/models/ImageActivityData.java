package se.tink.backend.common.workers.activity.generators.models;

public class ImageActivityData {
    private String action;
    private String documentId;
    private String helpText;
    private String icon;
    private boolean sharable;
    private String sharableMessage;
    private String url;

    public String getAction() {
        return action;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getHelpText() {
        return helpText;
    }

    public String getIcon() {
        return icon;
    }

    public String getSharableMessage() {
        return sharableMessage;
    }

    public String getUrl() {
        return url;
    }

    public boolean isSharable() {
        return sharable;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setSharable(boolean sharable) {
        this.sharable = sharable;
    }

    public void setSharableMessage(String sharableMessage) {
        this.sharableMessage = sharableMessage;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
