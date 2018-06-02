package se.tink.backend.core.application;

public class TextWithTitle {
    private String title;
    private String text;

    public static TextWithTitle of(String title, String text) {
        TextWithTitle textWithTitle = new TextWithTitle();
        textWithTitle.title = title;
        textWithTitle.text = text;
        return textWithTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
