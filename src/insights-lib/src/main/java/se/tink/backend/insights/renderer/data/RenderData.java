package se.tink.backend.insights.renderer.data;

import java.util.List;
import se.tink.backend.insights.core.valueobjects.Icon;

public class RenderData {
    private final String activityClass;
    private final Icon icon;
    private final String titleMessage;
    private final String bodyMessage;
    private final List<Button> buttons;

    public RenderData(String activityClass, Icon icon, String titleMessage, String bodyMessage, List<Button> buttons) {
        this.activityClass = activityClass;
        this.icon = icon;
        this.titleMessage = titleMessage;
        this.bodyMessage = bodyMessage;
        this.buttons = buttons;
    }

    public String getActivityClass() {
        return activityClass;
    }

    public Icon getIcon() {
        return icon;
    }

    public String getTitleMessage() {
        return titleMessage;
    }

    public String getBodyMessage() {
        return bodyMessage;
    }

    public List<Button> getButtons() {
        return buttons;
    }

    public static class Builder {

        private String activityClass;
        private Icon icon;
        private String titleMessage;
        private String bodyMessage;
        private List<Button> buttons;

        public RenderData build(){
            return new RenderData(
                    activityClass,
                    icon,
                    titleMessage,
                    bodyMessage,
                    buttons
                    );
        }

        public Builder setActivityClass(String activityClass) {
            this.activityClass = activityClass;
            return this;
        }

        public Builder setIcon(Icon icon) {
            this.icon = icon;
            return this;
        }

        public Builder setTitleMessage(String titleMessage) {
            this.titleMessage = titleMessage;
            return this;
        }

        public Builder setBodyMessage(String bodyMessage) {
            this.bodyMessage = bodyMessage;
            return this;
        }

        public Builder setButtons(List<Button> buttons) {
            this.buttons = buttons;
            return this;
        }

    }


}
