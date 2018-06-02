package se.tink.backend.insights.renderer.data;

public class Button {

    private String divClass;
    private String message;
    private String deepLink;

    Button(String divClass, String message, String deepLink) {
        this.divClass = divClass;
        this.message = message;
        this.deepLink = deepLink;
    }

    public static Button of(String divClass, String message, String deepLink) {
        return new Button(divClass, message, deepLink);
    }

    public String getDivClass() {
        return divClass;
    }

    public String getMessage() {
        return message;
    }

    public String getDeepLink() {
        return deepLink;
    }

}
