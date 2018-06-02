package se.tink.backend.common.workers.activity.generators.models;

public class CustomActivityData {
    private String action;
    private String helpText;
    private String icon;
    private boolean sharable;
    private String clickableMessage;
    private String url;
    private String source;
    private double height;

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isSharable() {
        return sharable;
    }

    public void setSharable(boolean sharable) {
        this.sharable = sharable;
    }

    public String getClickableMessage() {
        return clickableMessage;
    }

    public void setClickableMessage(String clickableMessage) {
        this.clickableMessage = clickableMessage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
