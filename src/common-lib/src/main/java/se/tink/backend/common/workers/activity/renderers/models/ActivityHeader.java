package se.tink.backend.common.workers.activity.renderers.models;

public class ActivityHeader {
    private Icon icon;
    private String leftHeader;
    private String leftSubtext;
    private String rightHeader;
    private String rightSubtext;
    private String deepLink;

    public String getRightSubtext() {
        return rightSubtext;
    }

    public void setRightSubtext(String rightSubheader) {
        this.rightSubtext = rightSubheader;
    }

    public String getRightHeader() {
        return rightHeader;
    }

    public void setRightHeader(String rightHeader) {
        this.rightHeader = rightHeader;
    }

    public String getLeftSubtext() {
        return leftSubtext;
    }

    public void setLeftSubtext(String leftSubtext) {
        this.leftSubtext = leftSubtext;
    }

    public String getLeftHeader() {
        return leftHeader;
    }

    public void setLeftHeader(String leftHeader) {
        this.leftHeader = leftHeader;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public void setDeepLink(String deepLink)
    {
        this.deepLink = deepLink;
    }

    public String getDeepLink()
    {
        return deepLink;
    }
}
