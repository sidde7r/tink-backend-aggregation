package se.tink.libraries.identity.model;

public class IdentityEventAnswer {
    private String text;
    private IdentityAnswerKey key;

    public IdentityEventAnswer(String text, IdentityAnswerKey key) {
        this.text = text;
        this.key = key;
    }

    public String getText() {
        return text;
    }

    public IdentityAnswerKey getKey() {
        return key;
    }
}
