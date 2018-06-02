package se.tink.backend.insights.core.valueobjects;

public class HtmlData {
    private final String activityDivClass;
    private final Icon icon;

    public HtmlData(String activityDivClass, Icon icon) {
        this.activityDivClass = activityDivClass;
        this.icon = icon;
    }

    public String getActivityDivClass() {
        return activityDivClass;
    }

    public Icon getIcon() {
        return icon;
    }
}
