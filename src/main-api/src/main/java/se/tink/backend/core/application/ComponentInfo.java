package se.tink.backend.core.application;

public class ComponentInfo {
    private String infoTitle;
    private String infoBody;

    public static ComponentInfo of(String infoTitle, String infoBody) {
        ComponentInfo textWithTitle = new ComponentInfo();
        textWithTitle.infoTitle = infoTitle;
        textWithTitle.infoBody = infoBody;
        return textWithTitle;
    }

    public String getInfoTitle() {
        return infoTitle;
    }

    public void setInfoTitle(String infoTitle) {
        this.infoTitle = infoTitle;
    }

    public String getInfoBody() {
        return infoBody;
    }

    public void setInfoBody(String infoBody) {
        this.infoBody = infoBody;
    }
}
