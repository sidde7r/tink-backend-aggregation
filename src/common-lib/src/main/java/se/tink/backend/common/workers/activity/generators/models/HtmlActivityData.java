package se.tink.backend.common.workers.activity.generators.models;

public class HtmlActivityData {
    private String activityHtml;
    private HtmlActivityIconData icon;
    private String trackingName;

    public String getActivityHtml() {
        return activityHtml;
    }

    public HtmlActivityIconData getIcon() {
        return icon;
    }

    public String getTrackingName() {
        return trackingName;
    }

    public void setActivityHtml(String activityHtml) {
        this.activityHtml = activityHtml;
    }

    public void setIcon(HtmlActivityIconData icon) {
        this.icon = icon;
    }

    public void setTrackingName(String trackingName) {
        this.trackingName = trackingName;
    }
}
