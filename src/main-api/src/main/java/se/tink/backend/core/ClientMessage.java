package se.tink.backend.core;

public class ClientMessage {

    private String locale;
    private String message;

    public String getLocale() {
        return locale;
    }

    public String getMessage() {
        return message;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
